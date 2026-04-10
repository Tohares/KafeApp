# KafeApp

Aplikace pro evidenci spotřeby kávy, mléka, cukru, kyseliny citronové a vyúčtování.

## Neintuitivní část GUI - v admin view Kafaři lze upravit počet káv naklikaných kafařem - dvojklik do buňky, případně klik a psát.

## Jak aplikaci otestovat (Vzorová data)
V repozitáři se nachází soubor `vzorova_data.backup`, který obsahuje předpřipravené uživatele, sklad a historii.
1. Spusťte aplikaci (App.java). Při prvním spuštění si vytvořte libovolného administrátora.
2. V horním menu zvolte **Aplikace -> Přihlásit** a přihlaste se jako vytvořený admin.
3. V horním menu zvolte **Zálohování -> Import dat ze zálohy** a vyberte soubor `vzorova_data.backup`. Zvolte "Plnou obnovu".
4. **POZOR:** Tímto importem se váš vytvořený administrátor přepíše tím ze zálohy!
5. Pro další přihlášení administrátora použijte login: **admin** a heslo: **admin**.
6. Předpřipravení kafaři jsou dle logiky login-heslo: Vaclav-vaclav, Honza-honza, Pavel-pavel, Marak-marak
