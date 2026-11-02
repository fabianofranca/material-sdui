# BFF numa instância do Lightsail

Passo a passo para uma instância **Ubuntu** (1 GB de RAM ou mais), acessada pela **PowerShell** com a chave `.pem`, e um **domínio** seu. No fim, o BFF responde em `https://bff.seudominio.com`.

Troque nos comandos:

- `bff.seudominio.com`: o seu domínio;
- `1.2.3.4`: o IP estático da instância;
- o caminho da chave `.pem`.

## 1. IP estático

No console do Lightsail: **Networking → Create static IP**, escolha a instância e crie. Sem IP estático, o IP muda quando a instância para e liga, e o domínio deixa de apontar para ela.

## 2. Firewall

Na instância: aba **Networking → IPv4 Firewall**. Deixe abertas:

| Aplicação | Porta | Para quê                                      |
| --------- | ----- | --------------------------------------------- |
| SSH       | 22    | acesso (já vem aberta)                        |
| HTTP      | 80    | emissão do certificado (já vem aberta)        |
| HTTPS     | 443   | **adicione esta**: é por onde os apps acessam |

Não abra a 8080: o BFF só escuta dentro da instância.

## 3. DNS

No painel do seu domínio, crie um registro **A**: nome `bff`, valor `1.2.3.4` (o IP estático). Confira na PowerShell (pode levar alguns minutos):

```powershell
Resolve-DnsName bff.seudominio.com
```

## 4. Chave `.pem` no Windows

O `ssh` recusa a chave se outros usuários puderem lê-la. Uma vez só:

```powershell
$key = "$HOME\Downloads\LightsailDefaultKey-us-east-1.pem"
icacls $key /inheritance:r
icacls $key /grant:r "$($env:USERNAME):(R)"
```

## 5. Gerar o pacote

Na raiz do repositório (precisa de JDK 17 ou mais novo):

```powershell
.\gradlew.bat :bff:distZip
```

O pacote sai em `bff\build\distributions\bff-0.1.0.zip`. O script de instalação é `deploy\lightsail\install.sh`.

## 6. Enviar os arquivos

```powershell
scp -i $key .\bff\build\distributions\bff-0.1.0.zip .\deploy\lightsail\install.sh ubuntu@1.2.3.4:~
```

## 7. Instalar

```powershell
ssh -i $key ubuntu@1.2.3.4
```

Já na instância:

```bash
sudo bash install.sh bff-0.1.0.zip bff.seudominio.com
```

O script instala o Java 21 e o Caddy, põe o BFF em `/opt/bff` como serviço (`bff`) e configura o HTTPS. Na primeira vez, leva uns 2 minutos.

## 8. Testar

Da sua máquina:

```powershell
curl.exe https://bff.seudominio.com/health
curl.exe https://bff.seudominio.com/screens/home
```

## 9. Apontar os apps

- Android: `.\gradlew.bat :androidApp:installDebug -Psdui.bffUrl=https://bff.seudominio.com`
- iOS: troque `baseUrl` em `iosApp/iosApp/ContentView.swift`.

## Atualizar

Gere o zip de novo, envie (passo 6) e rode o mesmo `sudo bash install.sh ...`. A versão anterior fica em `/opt/bff.old`.

## Se algo der errado

| Sintoma                                           | O que fazer                                                                                                     |
| ------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `$'\r': command not found` ao rodar o script      | O arquivo ganhou finais de linha do Windows. Na instância: `sed -i 's/\r$//' install.sh` e rode de novo.        |
| `UNPROTECTED PRIVATE KEY FILE` no `ssh`           | Refaça o passo 4.                                                                                               |
| O BFF não sobe                                    | `sudo journalctl -u bff -n 50`                                                                                  |
| `curl https://...` falha ou dá erro de certificado | Confira o DNS (passo 3) e a porta 443 (passo 2); depois `sudo journalctl -u caddy -n 50`.                      |
| Status dos serviços                               | `sudo systemctl status bff caddy`                                                                               |

Os dados de exemplo do BFF ficam em memória: somem quando o serviço reinicia. O BFF ainda não tem autenticação.
