#!/bin/bash

URL=$1
[[ -z $URL ]] && URL="http://localhost:8080"

URL="$URL/api/admin/teirepos/reimportFresher"

echo "will post to $URL ..."
curl -X POST $URL
echo "done."