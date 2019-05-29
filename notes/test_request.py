import httplib
import json
import requests

result = requests.get('http://0.0.0.0:5000')

data1 = result.read()
print (data1)
print (result.status, result.reason)


#url = "http://127.0.0.1:2214/notes"
#conn =  httplib.HTTPConnection('193.219.91.103:2214')
data = {"title": '#1', "author": 'name', "comment": 'sometext'}
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}

#conn.request("POST", "/notes", data = json.dumps(data), headers)
#result = conn.getresponse()

result = requests.post('http://193.219.91.103:2214/notes', data=json.dumps(data), headers=headers)
print (result)
