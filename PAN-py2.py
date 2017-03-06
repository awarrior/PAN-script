#-*- coding:utf-8 -*-
import uuid
import socket
import getpass
import urllib2
import urllib
import ssl
import sys
import os
ssl._create_default_https_context = ssl._create_unverified_context
reload(sys)
sys.setdefaultencoding('utf8')

host = '202.69.19.85:8443'
##localIP = socket.gethostbyname(socket.gethostname())
ipList = socket.gethostbyname_ex(socket.gethostname())
for ip in ipList:
    if type(ip) == list and len(ip) > 0:
        externalIP = ip[0]
        break
mac = ':'.join(("%012X" % uuid.getnode())[i:i+2] for i in range(0, 12, 2))
url = 'https://%s/portal.do?wlanacname=bgl-01&wlanuserip=%s&mac=%s&vlan=1' % (host,externalIP,mac)
##print(url)

###############
###goLoginForm
###############

###- parameters
params = {}
params['loginType'] = ''
params['auth_type'] = '0'
params['isBindMac'] = '0'
params['pageid'] = '1201'
params['templatetype'] = '2'
params['listbindmac'] = '0'
params['loginTimes'] = ''
params['groupId'] = ''
params['url'] = ''
##print(params)
params['userId'] = raw_input('userId: ')
params['passwd'] = getpass.getpass('password: ')
data = urllib.urlencode(params)
data = data.encode('utf-8')

###- headers
headers = {}
headers['Accept'] = 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8'
headers['Accept-Encoding'] = 'gzip, deflate'
headers['Accept-Language'] = 'zh-CN,zh;q=0.8'
headers['Connection'] = 'keep-alive'
headers['Host'] = host
headers['Upgrade-Insecure-Requests'] = '1'
##headers['Content-Length'] = '138'
headers['Cache-Control'] = 'max-age=0'
headers['Origin'] = 'https://'+host
headers['Content-Type'] = 'application/x-www-form-urlencoded'
headers['Referer'] = url
headers['User-Agent'] = 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'

###- request
req = urllib2.Request(url, data, headers)
print('[LOGIN] loading...')
resp = urllib2.urlopen(req)
page = resp.read().decode('utf-8')
if page.find('上网期间请不要关闭') == -1:
    print('[LOGIN] failed.')
    print('[LOGIN] retry again...')
    resp = urllib2.urlopen(req)
    page = resp.read().decode('utf-8')
    if page.find('上网期间请不要关闭') == -1:
        print('[LOGIN] failed.')
        print(page)
    else:
        print('[LOGIN] successful~')
else:
    print('[LOGIN] successful~')
os.system('pause')
