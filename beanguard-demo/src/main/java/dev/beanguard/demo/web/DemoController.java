package dev.beanguard.demo.web;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.api.models.licence.LicenceTransferInitRequest;
import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;
import dev.beanguard.api.models.shop.LicenceTokenResponse;
import dev.beanguard.client.annotations.RequiresLicenceFeature;
import dev.beanguard.client.annotations.RequiresLicenceLimit;
import dev.beanguard.client.config.LicenceKeys;
import dev.beanguard.client.config.ServerConfig;
import dev.beanguard.client.exceptions.LicenceLimitExceeded;
import dev.beanguard.client.exceptions.MissingLicenceFeature;
import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.registries.LicenceStatus;
import dev.beanguard.client.server.BeanGuardServer;
import dev.beanguard.client.usage.UsageRegistry;
import dev.beanguard.demo.licence.DemoLicenceKeyStore;
import dev.beanguard.demo.licence.DemoLicenceKeyStore.PendingTransfer;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.Optional;

@Controller
public class DemoController {

    private final LicenceRegistry licenceRegistry;
    private final BeanGuardServer beanGuardServer;
    private final DemoLicenceKeyStore licenceKeyStore;
    private final MessageSource messageSource;
    private final UsageRegistry usageRegistry;
    private final RestClient restClient = RestClient.create();

    public DemoController(LicenceRegistry licenceRegistry,
                           BeanGuardServer beanGuardServer,
                           DemoLicenceKeyStore licenceKeyStore,
                           MessageSource messageSource,
                           UsageRegistry usageRegistry) {
        this.licenceRegistry = licenceRegistry;
        this.beanGuardServer = beanGuardServer;
        this.licenceKeyStore = licenceKeyStore;
        this.messageSource = messageSource;
        this.usageRegistry = usageRegistry;
    }

    private String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    private DemoServerConfigForm prefilledServerConfigForm() {
        ServerConfig current = licenceKeyStore.getServerConfig();
        DemoServerConfigForm form = new DemoServerConfigForm();
        form.setUrl(current.getUrl());
        form.setPublicKey(current.getKey());
        // secretKey is intentionally left blank - never echo a stored secret back into the form
        return form;
    }

    @GetMapping("/")
    public String index(Model model) {
        LicenceStatus status = licenceRegistry.getStatus();
        model.addAttribute("status", status);
        if (!model.containsAttribute("serverConfigForm")) {
            model.addAttribute("serverConfigForm", prefilledServerConfigForm());
        }
        if (status == LicenceStatus.LOADED) {
            model.addAttribute("licence", licenceRegistry.getLicence());
            model.addAttribute("usersUsage", usageRegistry.getUsage("users"));
            model.addAttribute("usersLimit", licenceRegistry.getLimit("users"));
            return "index";
        }
        Optional<PendingTransfer> pendingTransfer = licenceKeyStore.loadPendingTransfer();
        if (pendingTransfer.isPresent()) {
            model.addAttribute("pendingTransfer", pendingTransfer.get());
        } else {
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", new DemoLicenceRequestForm());
            }
            if (!model.containsAttribute("transferForm")) {
                model.addAttribute("transferForm", new DemoLicenceTransferRequestForm());
            }
        }
        return "index";
    }

    @PostMapping("/server-config")
    public String updateServerConfig(@Valid @ModelAttribute("serverConfigForm") DemoServerConfigForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "serverConfigForm", bindingResult);
            redirectAttributes.addFlashAttribute("serverConfigForm", form);
            return "redirect:/";
        }
        licenceKeyStore.updateServerConfig(new ServerConfig(form.getUrl(), form.getPublicKey(), form.getSecretKey()));
        licenceRegistry.refreshLicence();
        return "redirect:/";
    }

    @PostMapping("/demo-licence")
    public String generateDemoLicence(@Valid @ModelAttribute("form") DemoLicenceRequestForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/";
        }
        try {
            Licence licence = beanGuardServer.createDemoLicence(
                    new LicenceDemoCreateRequest(form.getEmail(), form.getVatId()));
            licenceKeyStore.storeLicenceKeys(new LicenceKeys(licence.getKey().toString(), licence.getSecret()));
            licenceRegistry.refreshLicence();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", msg("demo.error.generate", e.getMessage()));
        }
        return "redirect:/";
    }

    @PostMapping("/refresh")
    public String refresh() {
        licenceRegistry.refreshLicence();
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String initiateTransfer(@Valid @ModelAttribute("transferForm") DemoLicenceTransferRequestForm form,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "transferForm", bindingResult);
            redirectAttributes.addFlashAttribute("transferForm", form);
            return "redirect:/";
        }
        try {
            LicenceTransferInitResponse response = restClient.post()
                    .uri(licenceKeyStore.getServerConfig().getUrl() + "/api/open/licences/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LicenceTransferInitRequest(form.getLicenceKey()))
                    .retrieve()
                    .body(LicenceTransferInitResponse.class);
            licenceKeyStore.storePendingTransfer(response.transferToken(), form.getLicenceKey(), response.expiresAt());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", msg("demo.error.transferInit", e.getMessage()));
        }
        return "redirect:/";
    }

    @PostMapping("/transfer/check")
    public String checkTransfer(RedirectAttributes redirectAttributes) {
        Optional<PendingTransfer> pending = licenceKeyStore.loadPendingTransfer();
        if (pending.isEmpty()) {
            return "redirect:/";
        }
        PendingTransfer transfer = pending.get();
        try {
            LicenceTransferStatusResponse response = restClient.get()
                    .uri(licenceKeyStore.getServerConfig().getUrl() + "/api/open/licences/transfer/" + transfer.token())
                    .retrieve()
                    .body(LicenceTransferStatusResponse.class);
            switch (response.status()) {
                case "CONFIRMED" -> {
                    licenceKeyStore.storeLicenceKeys(
                            new LicenceKeys(transfer.licenceKey().toString(), response.secret()));
                    licenceKeyStore.clearPendingTransfer();
                    licenceRegistry.refreshLicence();
                }
                case "EXPIRED" -> {
                    licenceKeyStore.clearPendingTransfer();
                    redirectAttributes.addFlashAttribute("error", msg("demo.error.transferExpired"));
                }
                default -> {
                    // still PENDING — the waiting view re-renders as-is, nothing to flash
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", msg("demo.error.transferCheck", e.getMessage()));
        }
        return "redirect:/";
    }

    @PostMapping("/transfer/cancel")
    public String cancelTransfer() {
        licenceKeyStore.clearPendingTransfer();
        return "redirect:/";
    }

    @GetMapping("/extend")
    public String extend(RedirectAttributes redirectAttributes) {
        Licence licence = licenceRegistry.getLicence();
        if (licence == null) {
            redirectAttributes.addFlashAttribute("error", msg("demo.error.noLicenceToExtend"));
            return "redirect:/";
        }
        try {
            String credentials = licence.getKey() + ":" + licence.getSecret();
            String authorization = "KeySecret " + Base64.getEncoder().encodeToString(credentials.getBytes());
            LicenceTokenResponse response = restClient.post()
                    .uri(licenceKeyStore.getServerConfig().getUrl() + "/api/open/licences/token")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(LicenceTokenResponse.class);
            return "redirect:" + response.getShopUrl();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", msg("demo.error.extend", e.getMessage()));
            return "redirect:/";
        }
    }

    @RequiresLicenceFeature("pdf-export")
    @GetMapping("/pdf-export")
    public ResponseEntity<Resource> pdfExport() {
        Resource pdf = new ClassPathResource("pdf/blank.pdf");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("export.pdf").build().toString())
                .body(pdf);
    }

    @ExceptionHandler(MissingLicenceFeature.class)
    public String handleMissingLicenceFeature(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", msg("demo.error.pdfExport"));
        return "redirect:/";
    }

    @RequiresLicenceLimit("users")
    @PostMapping("/add-user")
    public String addUser() {
        return "redirect:/";
    }

    @ExceptionHandler(LicenceLimitExceeded.class)
    public String handleLicenceLimitExceeded(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", msg("demo.error.limitExceeded"));
        return "redirect:/";
    }
}
