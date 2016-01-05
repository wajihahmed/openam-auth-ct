#!/bin/sh

#curl -v --request POST \
#--header 'Cookie: CTSESSION=123456789' \
#--data {} \
# http://mbp.wfoo.net:6080/openam/UI/Login?module=CTAuth

echo ""
echo "==> Testing without Cookie..."
curl -v --request POST \
--header 'Content-Type: application/json' \
 'http://mbp.wfoo.net:6080/openam/json/authenticate?module=CTAuth&authIndexType=module&authIndexValue=CTAuth'

echo ""
echo "==> Testing with Cookie..."
curl -v --request POST \
--header 'Content-Type: application/json' \
--header 'Cookie: CTSESSION=123456789' \
 'http://mbp.wfoo.net:6080/openam/json/authenticate?module=CTAuth&authIndexType=module&authIndexValue=CTAuth'

#curl 'http://mbp.wfoo.net:6080/openam/json/authenticate?module=CTAuth&authIndexType=module&authIndexValue=CTAuth' -X POST -H 'Accept-API-Version: protocol=1.0,resource=2.0' -H 'Cookie: JSESSIONID=BD86F7FE953D5B5987DAF510076B5AEC; i18next=en-US; amlbcookie=01' -H 'Origin: http://mbp.wfoo.net:6080' -H 'Accept-Encoding: gzip, deflate' -H 'X-Password: anonymous' -H 'Accept-Language: en-US,en;q=0.8' -H 'X-Requested-With: XMLHttpRequest' -H 'Connection: keep-alive' -H 'Content-Length: 0' -H 'Pragma: no-cache' -H 'X-Username: anonymous' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36' -H 'Content-Type: application/json' -H 'Accept: application/json, text/javascript, */*; q=0.01' -H 'Cache-Control: no-cache' -H 'Referer: http://mbp.wfoo.net:6080/openam/XUI/' -H 'X-NoSession: true' -H 'DNT: 1' --compressed
