package io.beanguard.demo.web;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.api.models.licence.LicenceDemoCreateRequest;
import io.beanguard.api.models.licence.LicenceTransferInitRequest;
import io.beanguard.api.models.licence.LicenceTransferInitResponse;
import io.beanguard.api.models.licence.LicenceTransferStatusResponse;
import io.beanguard.api.models.shop.LicenceTokenResponse;
import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.registries.LicenceRegistry;
import io.beanguard.client.registries.LicenceStatus;
import io.beanguard.client.server.BeanGuardServer;
import io.beanguard.demo.config.DemoProperties;
import io.beanguard.demo.licence.DemoLicenceKeyStore;
import io.beanguard.demo.licence.DemoLicenceKeyStore.PendingTransfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    private final DemoProperties properties;
    private final RestClient restClient = RestClient.create();

    public DemoController(LicenceRegistry licenceRegistry,
                           BeanGuardServer beanGuardServer,
                           DemoLicenceKeyStore licenceKeyStore,
                           DemoProperties properties) {
        this.licenceRegistry = licenceRegistry;
        this.beanGuardServer = beanGuardServer;
        this.licenceKeyStore = licenceKeyStore;
        this.properties = properties;
    }

    @GetMapping("/")
    public String index(Model model) {
        LicenceStatus status = licenceRegistry.getStatus();
        model.addAttribute("status", status);
        if (status == LicenceStatus.LOADED) {
            model.addAttribute("licence", licenceRegistry.getLicence());
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
            redirectAttributes.addFlashAttribute("error",
                    "Nie udało się wygenerować licencji demo: " + e.getMessage());
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
                    .uri(properties.getServer().getUrl() + "/api/open/licences/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LicenceTransferInitRequest(form.getLicenceKey()))
                    .retrieve()
                    .body(LicenceTransferInitResponse.class);
            licenceKeyStore.storePendingTransfer(response.transferToken(), form.getLicenceKey(), response.expiresAt());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Nie udało się rozpocząć przeniesienia licencji: " + e.getMessage());
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
                    .uri(properties.getServer().getUrl() + "/api/open/licences/transfer/" + transfer.token())
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
                    redirectAttributes.addFlashAttribute("error",
                            "Token przeniesienia wygasł. Spróbuj ponownie.");
                }
                default -> {
                    // still PENDING — the waiting view re-renders as-is, nothing to flash
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Nie udało się sprawdzić statusu przeniesienia: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("error", "Brak wczytanej licencji do przedłużenia.");
            return "redirect:/";
        }
        try {
            String credentials = licence.getKey() + ":" + licence.getSecret();
            String authorization = "KeySecret " + Base64.getEncoder().encodeToString(credentials.getBytes());
            LicenceTokenResponse response = restClient.post()
                    .uri(properties.getServer().getUrl() + "/api/open/licences/token")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(LicenceTokenResponse.class);
            return "redirect:" + response.getShopUrl();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Nie udało się połączyć ze sklepem: " + e.getMessage());
            return "redirect:/";
        }
    }
}
