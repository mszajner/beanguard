# Roadmap: publikacja na GitHub + Docker Hub + Maven Central

Cel: przenieść projekt z GitLaba na GitHub, publikować obrazy Docker (`server`, `admin`, `shop`, `docs`) na hub.docker.com, a `beanguard-api` i `beanguard-client` jako biblioteki na Maven Central.

## Stan obecny (ważne, zanim zaczniesz)

Duża część przygotowań **już jest zrobiona** (wg audytu z 2026-07-15, od tego czasu skasowanego jako nieaktualny — stan jest teraz śledzony tu i w `CHANGELOG.md`). W skrócie, co już stoi:

- `pom.xml` ma `groupId dev.beanguard` (zmienione z `io.github.mszajner` — zdecydowano się na własną domenę, zob. "Faza 3 (alternatywa)"), `<scm>`/`<url>` wskazują na `github.com/mszajner/beanguard`, usunięto stare `dav:` repo rexoft.
- `beanguard-api` i `beanguard-client` mają gotowy, opt-in profil Maven `release` (`mvn -Prelease deploy`) z `maven-source-plugin`, `maven-javadoc-plugin`, `maven-gpg-plugin` i `central-publishing-maven-plugin` (Sonatype Central Portal) — kod czeka tylko na konto i klucz GPG.
- Istnieje **działający** `.github/workflows/ci.yml`: testy Javy, testy trzech frontendów (macierz), i job `publish` budujący i wypychający wszystkie 4 obrazy Docker — **ale na `ghcr.io`, nie na Docker Hub**. To główna rzecz do zmiany w kodzie.
- Licencje (`LICENSE.md` + `licenses/*.txt`), README-y wszystkich modułów, `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, szablony issue/PR, `.env.example`, `docker-compose.yml` — gotowe.
- CI **nie zostało jeszcze odpalone naprawdę** (nie ma jeszcze repo na GitHubie) — pierwszy realny push będzie pierwszym testem całości.

Ten roadmap skupia się więc na tym, czego brakuje **konkretnie do Docker Hub + Maven Central + GitHub jako miejsca docelowego**, nie powtarza całego audytu.

Legenda: 🤖 = mogę to zrobić ja (zmiana w kodzie/configu w tym repo) · 🧍 = to możesz zrobić tylko Ty (konto, sekret, decyzja biznesowa/personalna).

---

## Faza 1 — GitHub jako główne repo

- [x] 🧍 Załóż (jeśli jeszcze nie istnieje) publiczne repo `github.com/mszajner/beanguard` — puste, bez inicjalizowania README/licencji (już je mamy).
- [x] 🧍 Zdecyduj: `main` czy inna nazwa domyślnego brancha (obecnie lokalnie `main` — pasuje do tego, co już jest w `pom.xml`/CI).
- [x] 🧍 Zmień `git remote` na nowe repo i wypchnij historię (`git remote set-url origin git@github.com:mszajner/beanguard.git` albo dodaj jako nowy remote i przepnij) — to jest operacja na Twoim koncie/kluczach SSH, nie zrobię tego za Ciebie.
- [x] 🧍 Zdecyduj, co z `.gitlab-ci.yml` — zostawić jako martwy plik, czy usunąć po migracji -> Usunąłem.
- [x] 🤖 Jeśli zdecydujesz się usunąć `.gitlab-ci.yml`, zrobię to (jedna linijka).
- [x] 🧍 Ustawienia repo na GitHubie (opcjonalnie, ale warto): opis, topics, ochrona brancha `main`, wymagane checki CI przed merge.
- [x] 🧍 Sprawdź `<organization>BeanGuard Sp. z o.o.</organization>` i dane developera w `pom.xml` — to trafi do publicznego POM-a na Maven Central. Potwierdź, że to ma być publiczne w tej formie (albo powiedz, co zmienić — to już mogę zrobić ja).

## Faza 2 — Docker Hub zamiast GHCR

- [x] 🧍 Załóż konto/organizację na hub.docker.com (jeśli jeszcze nie masz) — sugeruję organizację, nie konto prywatne, żeby obrazy były pod spójną nazwą niezależną od Twojego loginu.
- [x] 🧍 Wygeneruj Access Token: Docker Hub → Account Settings → Security → **New Access Token** (uprawnienia: Read & Write wystarczy, nie potrzeba Admin).
- [x] 🧍 Dodaj w GitHub repo → Settings → Secrets and variables → Actions dwa sekrety: `DOCKERHUB_USERNAME` i `DOCKERHUB_TOKEN`.
- [x] 🤖 Zmienię `publish` job w `.github/workflows/ci.yml`: `docker/login-action` z `ghcr.io` na Docker Hub (`registry` można wtedy w ogóle pominąć — to domyślny rejestr), `username`/`password` z nowych sekretów zamiast `github.actor`/`GITHUB_TOKEN`, i nazwę obrazu z `ghcr.io/${{ github.repository_owner }}/beanguard-X` na `mszajner/beanguard-X` (albo nazwę Twojej organizacji Docker Hub, jeśli inna niż login GitHub — powiedz jaką). *(Zrobione: nazwa obrazu czyta się teraz z sekretu `DOCKERHUB_USERNAME`, więc zadziała pod dowolną nazwą konta/organizacji bez dalszych zmian w kodzie.)*
- [x] 🤖 Podmienię 4 miejsca, które dziś zakładają `ghcr.io/mszajner/...`: `beanguard-admin/README.md`, `beanguard-shop/README.md`, plus placeholder `rexoft/beanguard-*` w treści `beanguard-docs` (`serwer`, `sklep`, `pobierz`, `szybki-start`, `panel-admina` — te strony już mają notatkę "obrazy nie są jeszcze opublikowane", więc to głównie kosmetyka do zrobienia po pierwszym udanym publish). *(Zrobione: wszystkie miejsca w PL+EN podmienione na `mszajner/beanguard-*`; notatka "obrazy nie są jeszcze opublikowane" zostawiona bez zmian, bo to wciąż prawda.)*

## Faza 3 — Maven Central (beanguard-api, beanguard-client)

- [x] 🧍 Załóż konto na **central.sonatype.com** (Central Portal — następca starego OSSRH).
- [x] 🧍 ~~Zweryfikuj namespace `io.github.mszajner` przez GitHub OAuth~~ — **nieaktualne**: zdecydowano się na własny groupId `dev.beanguard` (zob. "Faza 3 (alternatywa)" niżej), więc weryfikacja namespace'u odbywa się przez rekord TXT w DNS `beanguard.dev`, nie przez GitHub OAuth. Szczegóły kroku w sekcji alternatywy.
- [x] 🧍 Wygeneruj User Token: Central Portal → swój profil (avatar) → **Generate User Token** — dostaniesz parę username/password (to nie jest Twoje hasło do konta, tylko oddzielny token).
- [x] 🧍 Wygeneruj parę kluczy GPG do podpisywania artefaktów: `gpg --full-generate-key` (RSA 4096, bez wygasania albo z długim terminem). Wyślij klucz publiczny na keyserver: `gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>` (Central wymaga, żeby klucz publiczny był odnajdywalny na jakimś keyserverze).
- [x] 🧍 Wyeksportuj klucz prywatny do sekretu CI: `gpg --armor --export-secret-keys <KEY_ID>` — to trafi do `GPG_PRIVATE_KEY`.
- [x] 🧍 Dodaj w GitHub Secrets: `CENTRAL_USERNAME`, `CENTRAL_PASSWORD` (z User Tokena), `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.
- [x] 🤖 Dodam nowy job `publish-maven` do `.github/workflows/ci.yml` (albo osobny `release.yml`, do ustalenia), uruchamiany tylko na tagach wersji (`v*`), który:
  - użyje `actions/setup-java` z `server-id: central`, `server-username`/`server-password` i `gpg-private-key`/`gpg-passphrase` z sekretów (ten action potrafi sam wygenerować `~/.m2/settings.xml` i zaimportować klucz GPG — nie trzeba osobnej akcji ani ręcznego pliku),
  - odpali `mvn -B -Prelease -pl beanguard-api,beanguard-client deploy` (celowo ograniczone do tych dwóch modułów — `beanguard-server` i root `pom` nie mają `distributionManagement`, więc `mvn deploy` bez `-pl` po prostu wybuchnie na nich).
  *(Zrobione: job dodany w `ci.yml`, gated na `refs/tags/v*`, czyta sekrety `CENTRAL_USERNAME`/`CENTRAL_PASSWORD`/`GPG_PRIVATE_KEY`/`GPG_PASSPHRASE` — nie zadziała, dopóki ich nie dodasz, ale kod czeka gotowy.)*
- [x] 🧍 Zdecyduj: publikacja ręczna (`autoPublish: false` już tak jest ustawione — po wysłaniu trzeba **ręcznie zatwierdzić** publikację w Central Portal UI) czy w pełni automatyczna (`autoPublish: true` w `pom.xml`). Zostawiłbym ręczne zatwierdzanie na pierwsze wydania, potem można przełączyć.

## Faza 3 (alternatywa) — własny groupId zamiast io.github.mszajner

**Zdecydowane: `dev.beanguard`.** Kod już zmieniony (zob. checklisty niżej) — zostaje weryfikacja DNS po Twojej stronie.

Obecny plan (Faza 3 wyżej) zakładał `groupId io.github.mszajner`, zweryfikowany automatycznie przez GitHub OAuth — bez DNS, najprostsza ścieżka. Wybrana alternatywa: groupId oparty o domenę `beanguard.dev`, którą faktycznie posiadasz (`pom.xml` miał już `<organization><url>https://beanguard.dev</url>`, choć ten blok został od tego czasu usunięty z `pom.xml` — samo posiadanie domeny na weryfikację DNS to nie zmienia). To bardziej "brandowany" groupId, niezależny od loginu GitHub, ale wymaga innej ścieżki weryfikacji.

Warto wiedzieć: to nie pierwsza taka zmiana w tym projekcie — `CHANGELOG.md` pokazuje, że groupId był już raz zmieniony z `io.beanguard` na `io.github.mszajner` właśnie po to, żeby ominąć konieczność weryfikacji domeny. Wybór własnego groupId to więc częściowy powrót do tamtej koncepcji, tyle że z realnie posiadaną domeną zamiast `beanguard.io`.

### Czym różni się weryfikacja namespace'u

- [x] 🧍 W Central Portal → Namespaces → Add Namespace wybierz opcję weryfikacji przez domenę (nie przez GitHub). Dostaniesz losowy kod, który wpisujesz jako rekord **TXT** w DNS domeny `beanguard.dev` (dokładnie ta domena, w odwróconej notacji, jest już groupId — `dev.beanguard`).
- [x] 🧍 Musisz mieć dostęp do panelu DNS tej domeny (u rejestratora albo dostawcy DNS) — to wyłącznie Twoja operacja, nie mam do tego dostępu.
- [x] 🧍 Domena **nie musi niczego hostować** — wystarczy sama kontrola nad DNS, weryfikacja to tylko rekord TXT. Propagacja zwykle trwa od kilku minut do godziny, potem klikasz "Verify" w Central Portal.
- [x] 🧍 Reszta Fazy 3 (User Token, klucze GPG, sekrety w GitHub) zostaje bez zmian — różni się tylko ten jeden krok weryfikacji namespace'u.

### Co trzeba podmienić w kodzie

- [x] 🤖 `<groupId>` w `pom.xml` (root), `beanguard-api/pom.xml`, `beanguard-client/pom.xml`, `beanguard-server/pom.xml`.
- [x] 🤖 Wewnętrzne współrzędne zależności w `dependencyManagement` root `pom.xml` (referencje do `beanguard-api`/`beanguard-client` po starym groupId).
- [x] 🤖 Przykładowe `<dependency>` w dokumentacji: `beanguard-docs` — strony pobierz/download i integracja klienta (po jednym miejscu w PL i EN), plus `beanguard-client/README.md`.
- [x] 🤖 Wzmianki opisowe: `ROADMAP.md` (ten plik), `OPEN_SOURCE_CHECKLIST.md`, `CHANGELOG.md` (dopisany nowy wpis o zmianie, stary wpis `io.beanguard → io.github.mszajner` zostawiony bez zmian — to log historii).
- [x] 🤖 Komentarz w `beanguard-api/pom.xml` przy profilu `release` opisujący sposób weryfikacji namespace'u (GitHub OAuth → DNS TXT).

*(Zrobione: wszystkie miejsca podmienione na `dev.beanguard`. Zostaje tylko weryfikacja DNS po Twojej stronie — patrz checklisty wyżej.)*

Java package `io.beanguard.*` **nie musi się zmieniać** — groupId Mavena i nazwa pakietu Javy to dwie niezależne rzeczy, i już dziś są rozjechane (obecny groupId `io.github.mszajner`, pakiet `io.beanguard.*`). Nowy groupId (np. `dev.beanguard`) też może spokojnie współistnieć z pakietem `io.beanguard.*` bez żadnych zmian w plikach `.java`. Jeśli zależy Ci na pełnej spójności groupId ↔ nazwa pakietu, to osobna, dużo bardziej inwazyjna operacja (dotyka każdego pliku `.java` w `beanguard-api`/`beanguard-client`/`beanguard-server`) — możliwa, ale do rozważenia niezależnie od tej decyzji.

### Kiedy to zdecydować

Teraz, przed pierwszą publikacją, jest najlepszy moment — nic jeszcze nie jest opublikowane pod `io.github.mszajner`. Raz wydany artefakt zostaje na Maven Central na zawsze (nie da się go usunąć ani przenieść pod inny groupId), więc zmiana **po** pierwszym `deploy` oznacza start od zera pod nowym namespace i osierocone stare współrzędne widoczne publicznie.

### Co musisz zdecydować

- [x] 🧍 Zostajesz przy `io.github.mszajner` (Faza 3 wyżej, prostsze, bez DNS) czy przechodzisz na własną domenę? → **Własna domena.**
- [x] 🧍 Jeśli własna domena — dokładny string: `dev.beanguard`, `org.rexoft`, czy coś innego? → **`dev.beanguard`.**

## Faza 4 — Weryfikacja

- [x] 🧍 Zrób pierwszy realny push do GitHuba i obserwuj, czy `ci.yml` faktycznie przechodzi (checklist już to flagował jako nigdy nie przetestowane na żywo). *(Zweryfikowane 2026-08-11 przez `gh run list`: oba przebiegi na `main` zielone — `test-java` i `test-frontend` (macierz 3x) przechodzą, `publish`/`publish-maven` poprawnie pominięte, bo to nie jest push taga.)*
- [x] 🧍 Wypchnij pierwszy tag (np. `v0.1.0`) i sprawdź, czy obrazy trafiają na Docker Hub, a `deploy` faktycznie wysyła paczkę do Central Portal (i czy trzeba ją tam ręcznie kliknąć "Publish"). *(Zrobione 2026-08-11: tag `v0.1.0` wypchnięty. Docker Hub — sukces, wszystkie 4 obrazy opublikowane. Maven Central — deployment odrzucony przez walidację Central (brak `<url>`/`<scm>`/`<developers>` i wersji zależności w surowym, niespłaszczonym pom.xml modułów — Central sprawdza wgrany plik, nie efektywny pom z dziedziczeniem). Naprawione dodaniem `flatten-maven-plugin` do profilu `release` — wymaga kolejnego taga (np. `v0.1.1`), żeby ponowić próbę.)*
- [x] 🤖 Jeśli coś w CI nie zadziała za pierwszym razem — pomogę to naprawić na podstawie logów z GitHub Actions. *(Dwie awarie `publish-maven` naprawione: brakujące metadane w pom (flatten-maven-plugin) i przestarzały `central-publishing-maven-plugin` 0.7.0 → 0.11.0 (crash przy deserializacji odpowiedzi API Central).)*
- [x] 🧍 Ręcznie zatwierdź deployment w Central Portal UI (bo `autoPublish: false`) — zrobione dla `v0.1.1`: `dev.beanguard:beanguard-api:0.1.1` i `beanguard-client:0.1.1` zweryfikowane na `repo1.maven.org` (jar/sources/javadoc/pom.asc, wszystkie 200) — **pierwszy pełny, zielony release: Docker Hub + Maven Central.**
- [x] 🤖 Dodam do README badge'y (build status, wersja Maven Central, Docker pulls). *(Zrobione: 3 badge'e shields.io w `README.md` — CI, Maven Central version, Docker Pulls. Uwaga: CI badge nie wyrenderuje się nikomu z zewnątrz, dopóki nie zrobisz kolejnego punktu niżej.)*
- [ ] 🧍 **Repo na GitHubie jest wciąż prywatne** (`gh api repos/mszajner/beanguard` → `"private": true`) — mimo że Docker Hub i Maven Central są już publiczne. Cały ten roadmap zakładał publikację na GitHub jako cel, ale nikt jeszcze nie przełączył widoczności repo. Ustaw na public w Settings → General → Danger Zone (albo `gh repo edit mszajner/beanguard --visibility public`), kiedy będziesz gotów — to świadoma decyzja, nie zrobię tego bez Twojego potwierdzenia.

---

## Skrócone TL;DR — co robimy w jakiej kolejności

1. **Ty**: GitHub repo + push (Faza 1).
2. **Ty**: konto Docker Hub + access token → sekrety w GitHub (Faza 2, pierwsza połowa).
3. **Ja**: przepinam CI z GHCR na Docker Hub (Faza 2, druga połowa) — mogę zrobić od razu, jak tylko podasz nazwę organizacji/konta Docker Hub.
4. **Ty**: konto Sonatype Central Portal + weryfikacja namespace `dev.beanguard` przez DNS (TXT record na `beanguard.dev`) + GPG + sekrety (Faza 3, pierwsza połowa) — to najbardziej czasochłonna, ręczna część. Decyzja o groupId już podjęta (patrz "Faza 3 (alternatywa)"), kod przepięty.
5. **Ja**: dopisuję job publikujący na Maven Central (Faza 3, druga połowa) — mogę zrobić od razu, nie musi czekać na Twoje sekrety (job po prostu nie zadziała bez nich, dopóki ich nie dodasz).
6. **Oboje**: pierwszy realny przebieg i poprawki (Faza 4).

Punkty 3 i 5 mogę zrobić **teraz, bez czekania na Ciebie** — daj znać, czy zaczynać, i jaka ma być nazwa organizacji/konta na Docker Hub (jeśli inna niż `mszajner`).
