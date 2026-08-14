package io.beanguard.server.models;

import lombok.Getter;

public enum ParameterName {
    TOKEN_EXPIRATION("86400000"),
    TOKEN_PRIVATE_KEY(""),
    TOKEN_PUBLIC_KEY(""),
    TOKEN_SECRET_KEY(""),
    TOKEN_ISSUER("BeanGuard"),
    LICENCE_PRIVATE_KEY(""),
    LICENCE_PUBLIC_KEY(""),
    LICENCE_SECRET_KEY(""),
    LICENCE_ISSUER("BeanGuard"),
    LICENCE_DEMO_CLAIMS("{\"users\":\"1\"}"),
    LICENCE_DEMO_EXPIRATION_DAYS("30"),
    MAIL_HOST("mailpit"),
    MAIL_PORT("1025"),
    MAIL_USER("any-user"),
    MAIL_PASS("any-password"),
    MAIL_FROM("noreply@beanguard.dev"),
    MAIL_SENDER_NAME("BeanGuard"),
    MAIL_SMTP_STARTTLS("true"),
    MAIL_DEMO_WELCOME_SUBJECT("Twoja licencja demo {{productName}}"),
    MAIL_DEMO_WELCOME_BODY("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Twoja licencja demo {{productName}}</title>
            <!--[if mso]>
            <noscript>
            <xml>
            <o:OfficeDocumentSettings>
            <o:PixelsPerInch>96</o:PixelsPerInch>
            </o:OfficeDocumentSettings>
            </xml>
            </noscript>
            <![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
                .h1 { font-size: 22px !important; line-height: 28px !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Tw&oacute;j klucz licencji demo {{productName}} jest gotowy &mdash; wa&zdot;ny do {{expirationDate}}.
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 16px 40px;">
                        <h1 class="h1" style="margin:0 0 16px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:26px; line-height:32px; font-weight:700; color:#111827;">
                          Twoja licencja demo {{productName}}
                        </h1>
                        <p style="margin:0 0 20px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:16px; line-height:24px; color:#4b5563;">
                          Dziękujemy za zainteresowanie! Oto Twój klucz licencji demo — podaj go w konfiguracji aplikacji, aby aktywować dostęp.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 24px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f9fafb; border:1px solid #eef0f3; border-radius:8px;">
                          <tr>
                            <td style="padding:16px 18px; font-family:'Courier New',Courier,monospace; font-size:14px; line-height:20px; color:#111827; word-break:break-all;">
                              {{licenceToken}}
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 32px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#fef3c7; border-radius:8px;">
                          <tr>
                            <td style="padding:14px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; line-height:20px; color:#92400e;">
                              <strong>Ważność:</strong> {{expirationDate}}
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 40px 40px; border-top: 1px solid #eef0f3;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin-top:24px;">
                          <tr>
                            <td style="padding-bottom:16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; line-height:22px; color:#374151;">
                              <strong style="color:#111827;">1. Skopiuj klucz</strong><br />
                              Zaznacz i skopiuj klucz z pola powyżej.
                            </td>
                          </tr>
                          <tr>
                            <td style="padding-bottom:16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; line-height:22px; color:#374151;">
                              <strong style="color:#111827;">2. Wklej w konfiguracji aplikacji</strong><br />
                              Wprowadź go w miejscu przeznaczonym na klucz licencyjny.
                            </td>
                          </tr>
                          <tr>
                            <td style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; line-height:22px; color:#374151;">
                              <strong style="color:#111827;">3. Uruchom aplikację</strong><br />
                              Licencja aktywuje się automatycznie przy pierwszym starcie.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{companyAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    MAIL_EXPIRY_WARNING_SUBJECT("Uwaga: licencja {{productName}} wygasa za {{daysLeft}} dni"),
    MAIL_EXPIRY_WARNING_BODY("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Licencja wygasa wkrótce</title>
            <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Twoja licencja {{productName}} wygasa za {{daysLeft}} dni ({{expirationDate}}).
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 8px 40px;">
                        <h1 style="margin:0 0 16px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:24px; line-height:30px; font-weight:700; color:#111827;">
                          Twoja licencja wygasa wkrótce
                        </h1>
                        <p style="margin:0 0 20px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:16px; line-height:24px; color:#4b5563;">
                          Drogi/a {{buyerCompany}}, Twoja licencja {{productName}} ({{email}}) wygaśnie za <strong>{{daysLeft}} dni</strong> — {{expirationDate}}. Aby zapewnić ciągłość działania oprogramowania, odnów licencję przed tą datą.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 8px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#fef3c7; border-radius:8px;">
                          <tr>
                            <td style="padding:14px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; line-height:20px; color:#92400e;">
                              Po wygaśnięciu licencji dostęp do oprogramowania zostanie zablokowany.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{companyAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    MAIL_EXPIRY_WARNING_SUBJECT_DEMO("Uwaga: licencja demo {{productName}} wygasa za {{daysLeft}} dni"),
    MAIL_EXPIRY_WARNING_BODY_DEMO("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Licencja demo wygasa wkrótce</title>
            <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Twoja licencja demo {{productName}} wygasa za {{daysLeft}} dni ({{expirationDate}}).
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 8px 40px;">
                        <h1 style="margin:0 0 16px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:24px; line-height:30px; font-weight:700; color:#111827;">
                          Twoja licencja demo wygasa wkrótce
                        </h1>
                        <p style="margin:0 0 20px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:16px; line-height:24px; color:#4b5563;">
                          Twoja licencja demo {{productName}} ({{email}}) wygaśnie za <strong>{{daysLeft}} dni</strong> — {{expirationDate}}. Skontaktuj się z nami, aby uzyskać pełną licencję i zachować dostęp do oprogramowania.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 8px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#fef3c7; border-radius:8px;">
                          <tr>
                            <td style="padding:14px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; line-height:20px; color:#92400e;">
                              Po wygaśnięciu licencji demo dostęp do oprogramowania zostanie zablokowany.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{companyAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    MAIL_EXPIRY_WARNING_DAYS("30,7"),
    MAIL_TRANSFER_CONFIRM_SUBJECT("Potwierdzenie przeniesienia licencji {{productName}}"),
    MAIL_TRANSFER_CONFIRM_BODY("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Potwierdzenie przeniesienia licencji</title>
            <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Ktoś próbował aktywować Twoją licencję {{productName}} na nowym urządzeniu.
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 8px 40px;">
                        <h1 style="margin:0 0 16px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:24px; line-height:30px; font-weight:700; color:#111827;">
                          Przeniesienie licencji {{productName}}
                        </h1>
                        <p style="margin:0 0 20px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:16px; line-height:24px; color:#4b5563;">
                          Ktoś próbował aktywować Twoją licencję {{productName}} na nowym urządzeniu. Jeżeli to byłeś/aś Ty, kliknij przycisk poniżej — stara instalacja przestanie działać.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" align="left" style="padding: 0 40px 24px 40px;">
                        <table role="presentation" cellpadding="0" cellspacing="0">
                          <tr>
                            <td style="border-radius:8px; background-color:{{accentColor}};">
                              <a href="{{confirmUrl}}" style="display:inline-block; padding:14px 28px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:16px; font-weight:600; color:#ffffff; text-decoration:none; border-radius:8px;">
                                Potwierdź przeniesienie
                              </a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 8px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#fef3c7; border-radius:8px;">
                          <tr>
                            <td style="padding:14px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; line-height:20px; color:#92400e;">
                              Jeżeli to NIE byłeś/aś Ty, zignoruj tę wiadomość — licencja pozostanie bez zmian.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 24px 40px 40px 40px;">
                        <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:13px; line-height:20px; color:#9ca3af;">
                          Jeśli przycisk nie działa, skopiuj i wklej ten adres do przeglądarki:<br />
                          <a href="{{confirmUrl}}" style="color:#6b7280; word-break:break-all;">{{confirmUrl}}</a>
                        </p>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{companyAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    MAIL_LICENCE_CREATED_SUBJECT("Twoja licencja {{productName}} jest gotowa"),
    MAIL_LICENCE_CREATED_BODY("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Twoja licencja jest gotowa</title>
            <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Twoja licencja {{productName}} jest aktywna. Poniżej znajdziesz klucz licencji.
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 8px 40px;">
                        <h1 style="margin:0 0 16px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:24px; line-height:30px; font-weight:700; color:#111827;">
                          Twoja licencja {{productName}} jest gotowa
                        </h1>
                        <p style="margin:0 0 20px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:16px; line-height:24px; color:#4b5563;">
                          Administrator aktywował Twoją licencję {{productName}}. Poniżej znajdziesz klucz licencji — podaj go w konfiguracji aplikacji, aby aktywować dostęp.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 24px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f9fafb; border-radius:8px; border:1px solid #e5e7eb;">
                          <tr>
                            <td style="padding:16px 20px; font-family:'Courier New',Courier,monospace; font-size:14px; line-height:22px; color:#111827; word-break:break-all; letter-spacing:0.04em;">
                              {{licenceKey}}
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{companyAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    MAIL_ORDER_CONFIRM_SUBJECT("Potwierdzenie zamówienia #{{orderId}}"),
    MAIL_ORDER_CONFIRM_BODY("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Potwierdzenie zamówienia</title>
            <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
                .detail-row td { display:block !important; width:100% !important; padding: 4px 0 !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Zamówienie #{{orderId}} zostało przyjęte do realizacji.
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 8px 40px;">
                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
                          <tr>
                            <td style="background-color:#d1fae5; border-radius:20px; padding:4px 12px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; font-weight:600; color:#065f46; text-transform:uppercase; letter-spacing:0.03em;">
                              Przyjęte
                            </td>
                          </tr>
                        </table>
                        <h1 style="margin:0 0 12px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:22px; line-height:28px; font-weight:700; color:#111827;">
                          Zamówienie #{{orderId}} przyjęte
                        </h1>
                        <p style="margin:0 0 24px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; line-height:22px; color:#4b5563;">
                          Dziękujemy za złożenie zamówienia. Zostało ono przyjęte do realizacji — skontaktujemy się z Tobą e-mailem po jego zatwierdzeniu.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 24px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f9fafb; border-radius:8px;">
                          <tr class="detail-row">
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; width:40%; border-bottom:1px solid #eef0f3;">Numer zamówienia</td>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#111827; font-weight:600; border-bottom:1px solid #eef0f3;">#{{orderId}}</td>
                          </tr>
                          <tr class="detail-row">
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; width:40%; border-bottom:1px solid #eef0f3;">Data złożenia</td>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#111827; font-weight:600; border-bottom:1px solid #eef0f3;">{{orderDate}}</td>
                          </tr>
                          <tr class="detail-row">
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; width:40%;">Okres licencji</td>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#111827; font-weight:600;">{{period}}</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 8px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border-radius:8px; overflow:hidden; border:1px solid #eef0f3;">
                          <thead>
                            <tr style="background-color:#f9fafb;">
                              <th style="padding:10px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:13px; font-weight:600; color:#6b7280; text-align:left;">Produkt</th>
                              <th style="padding:10px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:13px; font-weight:600; color:#6b7280; text-align:right;">Wartość netto</th>
                            </tr>
                          </thead>
                          <tbody>
                            {{itemsTable}}
                          </tbody>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 24px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td style="padding:8px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; border-bottom:1px solid #eef0f3;">Netto</td>
                            <td style="padding:8px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#374151; text-align:right; border-bottom:1px solid #eef0f3;">{{nettoTotal}} {{currency}}</td>
                          </tr>
                          <tr>
                            <td style="padding:8px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; border-bottom:1px solid #eef0f3;">VAT {{vatRate}}%</td>
                            <td style="padding:8px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#374151; text-align:right; border-bottom:1px solid #eef0f3;">{{vatAmount}} {{currency}}</td>
                          </tr>
                          <tr>
                            <td style="padding:10px 0 0 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; font-weight:700; color:#111827;">Łącznie brutto</td>
                            <td style="padding:10px 0 0 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; font-weight:700; color:#111827; text-align:right;">{{bruttoTotal}} {{currency}}</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 24px 40px; border-top:1px solid #eef0f3;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin-top:24px;">
                          <tr>
                            <td style="width:50%; padding-right:12px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:13px; line-height:20px; color:#6b7280; vertical-align:top;">
                              <div style="font-weight:600; color:#374151; margin-bottom:4px;">Nabywca</div>
                              {{buyerCompany}}<br />NIP: {{buyerVatId}}<br />{{buyerEmail}}
                            </td>
                            <td style="width:50%; padding-left:12px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:13px; line-height:20px; color:#6b7280; vertical-align:top;">
                              <div style="font-weight:600; color:#374151; margin-bottom:4px;">Sprzedawca</div>
                              {{sellerName}}<br />{{sellerNip}}<br />{{sellerAddress}}<br />{{sellerContact}}
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{sellerAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    MAIL_ORDER_CANCEL_SUBJECT("Zamówienie #{{orderId}} zostało anulowane"),
    MAIL_ORDER_CANCEL_BODY("""
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pl">
            <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <title>Zamówienie anulowane</title>
            <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->
            <style>
              @media (max-width: 620px) {
                .email-container { width: 100% !important; }
                .stack-padding { padding-left: 20px !important; padding-right: 20px !important; }
              }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;">
            <div style="display:none; max-height:0; overflow:hidden; mso-hide:all;">
              Zamówienie #{{orderId}} z dnia {{orderDate}} zostało anulowane.
            </div>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;">
              <tr>
                <td align="center" style="padding: 32px 16px;">
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td align="center" style="padding: 28px 24px; background-color:#ffffff; border-bottom: 1px solid #eef0f3;">
                        <img src="{{logoUrl}}" alt="Logo" width="140" style="display:block; height:auto; border:0; max-width:140px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="height:4px; line-height:4px; font-size:0; background-color:{{accentColor}};">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 40px 40px 8px 40px;">
                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
                          <tr>
                            <td style="background-color:#fee2e2; border-radius:20px; padding:4px 12px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; font-weight:600; color:#991b1b; text-transform:uppercase; letter-spacing:0.03em;">
                              Anulowane
                            </td>
                          </tr>
                        </table>
                        <h1 style="margin:0 0 12px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:22px; line-height:28px; font-weight:700; color:#111827;">
                          Zamówienie #{{orderId}} anulowane
                        </h1>
                        <p style="margin:0 0 24px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:15px; line-height:22px; color:#4b5563;">
                          Twoje zamówienie złożone {{orderDate}} zostało anulowane. Jeśli masz pytania, skontaktuj się z nami.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td class="stack-padding" style="padding: 0 40px 32px 40px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f9fafb; border-radius:8px;">
                          <tr>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; width:40%; border-bottom:1px solid #eef0f3;">Numer zamówienia</td>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#111827; font-weight:600; border-bottom:1px solid #eef0f3;">#{{orderId}}</td>
                          </tr>
                          <tr>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#6b7280; width:40%;">Data złożenia</td>
                            <td style="padding:12px 16px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:14px; color:#111827; font-weight:600;">{{orderDate}}</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                  <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px;">
                    <tr>
                      <td class="stack-padding" align="center" style="padding: 24px 40px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; font-size:12px; line-height:18px; color:#9ca3af;">
                        {{companyName}} &middot; {{companyAddress}}
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            </body>
            </html>
            """),
    RATE_LIMIT_OPEN_RPM("20"),
    BRANDING_TITLE("BeanGuard"),
    BRANDING_LOGO_URL(""),
    BRANDING_WEBSITE_URL(""),
    BRANDING_FAVICON_URL(""),
    BRANDING_SUPPORT_EMAIL(""),
    BRANDING_PRIMARY_COLOR("#f59e0b"),
    LEGAL_CURRENCY("PLN"),
    LEGAL_TERMS_URL(""),
    LEGAL_PRIVACY_URL(""),
    LEGAL_VAT_RATE("23"),
    LEGAL_COMPANY_NAME(""),
    LEGAL_COMPANY_NIP(""),
    LEGAL_COMPANY_KRS(""),
    LEGAL_COMPANY_ADDRESS(""),
    LEGAL_COMPANY_PHONE(""),
    LEGAL_PRO_FORMA_PAYMENT_DAYS("14"),
    LEGAL_BANK_ACCOUNT(""),
    LEGAL_CERTIFICATE_ACTIVATION_TEXT("Podczas pierwszego uruchomienia aplikacji na nowym komputerze wprowadź poniższy klucz licencji, aby aktywować kopię programu."),
    LEGAL_CERTIFICATE_TEMPLATE("""
            <?xml version="1.0" encoding="UTF-8"?>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
              <title>Certyfikat Licencji</title>
              <style type="text/css">
                body { font-family: 'DejaVu Sans', Arial, sans-serif; font-size: 10pt; color: #222;
                       margin: 1cm 1.5cm; }
                h1 { text-align: center; font-size: 15pt; font-weight: bold; margin: 0 0 12pt 0; }
                p  { margin: 3pt 0; line-height: 1.4; }
                .disclaimer { font-size: 8pt; color: #444; margin-bottom: 16pt; }
                .section-header { font-weight: bold; font-size: 10pt; padding-bottom: 2pt;
                                  border-bottom: 0.75pt solid #222; margin-top: 14pt; margin-bottom: 6pt; }
                table.details { width: 100%; border-collapse: collapse; }
                table.details td { padding: 2pt 0; vertical-align: top; }
                table.details td.lbl { width: 38%; color: #555; }
                .key-mono { font-family: 'DejaVu Sans Mono', monospace; font-size: 10pt; margin-top: 4pt; }
                .url-link  { color: #555; text-decoration: none; font-size: 8pt; }
              </style>
            </head>
            <body>
              <table style="width:100%;margin-bottom:20pt;">
                <tr>
                  <td style="vertical-align:middle;">{{logoSection}}</td>
                  <td style="text-align:right;vertical-align:top;">
                    <a class="url-link" href="{{websiteUrl}}">{{websiteUrl}}</a>
                  </td>
                </tr>
              </table>

              <h1>{{sellerName}} &#8212; Certyfikat Licencji</h1>

              <p class="disclaimer">
                WA&#379;NE: NINIEJSZY DOKUMENT PO&#346;WIADCZA PRAWO DO U&#379;YTKOWANIA OPROGRAMOWANIA
                PRZYZNANEGO PRZEZ {{sellerName}}. PROSIMY O ZACHOWANIE KOPII NINIEJSZEGO DOKUMENTU.
              </p>

              <div class="section-header">SZCZEG&#211;&#321;Y LICENCJI</div>
              <table class="details">
                <tr><td class="lbl">Klucz:</td><td class="key-mono">{{licenceKey}}</td></tr>
                <tr><td class="lbl">Typ:</td><td>Licencja komercyjna</td></tr>
                <tr><td class="lbl">Data wystawienia:</td><td>{{issueDate}}</td></tr>
                {{periodRow}}
                <tr><td class="lbl">Wa&#380;na do:</td><td>{{expiration}}</td></tr>
                {{claimsSection}}
              </table>

              <div class="section-header">AKTYWACJA OPROGRAMOWANIA</div>
              <p>{{activationText}}</p>

              <div class="section-header">LICENCJOBIORCA</div>
              <table class="details">
                <tr><td class="lbl">Nazwa:</td><td>{{companyName}}</td></tr>
                <tr><td class="lbl">NIP:</td><td>{{vatId}}</td></tr>
              </table>

              <div class="section-header">LICENCJONOWANE OPROGRAMOWANIE</div>
              <table class="details">
                <tr><td class="lbl">Nazwa:</td><td>{{productName}}</td></tr>
                <tr><td class="lbl">Wersja:</td><td>Wszelkie wydania produkt&#243;w udost&#281;pnione w okresie u&#380;ytkowania</td></tr>
                <tr><td class="lbl">Pobieranie:</td><td>Oprogramowanie jest dostarczane w formie elektronicznej i mo&#380;na je pobra&#263; ze strony: <a href="{{websiteUrl}}">{{websiteUrl}}</a></td></tr>
              </table>

              <div class="section-header">UWAGI</div>
              <p>Klucz licencji identyfikuje licencj&#281; przy odnowieniach i aktualizacjach.
                 Nie udost&#281;pniaj klucza osobom trzecim.</p>
            </body>
            </html>
            """),
    LEGAL_PRO_FORMA_TEMPLATE("""
            <?xml version="1.0" encoding="UTF-8"?>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
              <title>Faktura Pro-Forma {{number}}</title>
              <style type="text/css">
                body { font-family: 'DejaVu Sans', Arial, sans-serif; font-size: 10pt; color: #222; margin: 1cm; }
                h1 { text-align: center; font-size: 14pt; letter-spacing: 1pt; margin-bottom: 3pt; }
                .meta { text-align: center; font-size: 9pt; color: #666; margin-bottom: 14pt; }
                table.parties { width: 100%; margin-bottom: 14pt; }
                table.parties td { width: 50%; vertical-align: top; padding-right: 10pt; }
                .box-label { font-size: 8pt; font-weight: bold; text-transform: uppercase; color: #888;
                             border-bottom: 0.5pt solid #bbb; padding-bottom: 2pt; margin-bottom: 4pt; }
                table.items { width: 100%; border-collapse: collapse; font-size: 9pt; margin-bottom: 12pt; }
                table.items th { background-color: #f0f0f0; border: 0.5pt solid #bbb; padding: 4pt 5pt; }
                table.items td { border: 0.5pt solid #ccc; padding: 3pt 5pt; }
                table.summary { width: 200pt; margin-left: auto; font-size: 10pt; }
                table.summary td { padding: 2pt 5pt; }
                tr.total td { font-weight: bold; border-top: 0.5pt solid #333; }
                div.payment { margin-top: 20pt; padding: 8pt; border: 0.5pt solid #ccc; font-size: 9pt; }
                div.footer { margin-top: 20pt; font-size: 8pt; color: #888;
                             border-top: 0.5pt solid #ddd; padding-top: 6pt; text-align: center; }
                div.licence-info { margin-bottom: 10pt; padding: 5pt 8pt; border: 0.5pt solid #ddd;
                                   background-color: #fafafa; font-size: 9pt; }
              </style>
            </head>
            <body>
              <h1>FAKTURA PRO-FORMA</h1>
              <p class="meta">Nr {{number}} &#183; Data wystawienia: {{issueDate}} &#183; Termin p&#322;atno&#347;ci: {{paymentDue}}</p>
              <table class="parties">
                <tr>
                  <td>
                    <div class="box-label">Sprzedawca</div>
                    <strong>{{sellerName}}</strong><br/>
                    NIP: {{sellerNip}}<br/>
                    {{sellerKrsLine}}
                    {{sellerAddress}}<br/>
                    tel. {{sellerPhone}}
                  </td>
                  <td>
                    <div class="box-label">Nabywca</div>
                    <strong>{{buyerCompany}}</strong><br/>
                    NIP: {{buyerNip}}<br/>
                    {{buyerStreet}}<br/>
                    {{buyerPostCode}} {{buyerCity}}<br/>
                    {{buyerEmail}}
                  </td>
                </tr>
              </table>
              <table class="items">
                <thead>
                  <tr>
                    <th style="text-align:left">Us&#322;uga / towar</th>
                    <th style="text-align:center;width:6%">Ilo&#347;&#263;</th>
                    <th style="text-align:right;width:12%">Cena netto</th>
                    <th style="text-align:center;width:6%">VAT</th>
                    <th style="text-align:right;width:12%">Warto&#347;&#263; netto</th>
                    <th style="text-align:right;width:12%">Kwota VAT</th>
                    <th style="text-align:right;width:12%">Brutto</th>
                  </tr>
                </thead>
                <tbody>{{itemsTable}}</tbody>
              </table>
              <table class="summary">
                <tr><td>Razem netto:</td><td style="text-align:right">{{nettoTotal}} {{currency}}</td></tr>
                <tr><td>VAT {{vatRate}}%:</td><td style="text-align:right">{{vatAmount}} {{currency}}</td></tr>
                <tr class="total">
                  <td>Do zap&#322;aty brutto:</td>
                  <td style="text-align:right">{{bruttoTotal}} {{currency}}</td>
                </tr>
              </table>
              {{licenceSection}}
              <div class="payment">
                <strong>P&#322;atno&#347;&#263;</strong> &#8212; termin: {{paymentDue}}<br/>
                Numer konta: {{bankAccount}}<br/>
                Tytu&#322; przelewu: Faktura Pro-Forma nr {{number}}
              </div>
              <div class="footer">
                Niniejszy dokument nie stanowi faktury VAT. Po zatwierdzeniu zam&#243;wienia zostanie wystawiona faktura VAT.
              </div>
            </body>
            </html>
            """);

    @Getter
    private final String defaultValue;

    ParameterName(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
