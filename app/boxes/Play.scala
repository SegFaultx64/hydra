package boxes
import java.io._

class Play(val name: String, val path: File, val readme: String, val port: String, var running: Boolean = false) {
	val url = name + ".devBox"

	val config = s"""

<VirtualHost *:80>
  ServerAdmin none@none.none
  DocumentRoot "/usr/docs"
  ServerName url

  ProxyRequests Off
  ProxyVia Off

  ProxyPass / http://127.0.0.1:$port/
  ProxyPassReverse / http://127.0.0.1:$port/
</VirtualHost>
	"""

	def foward() = {
		general.Config.sudoWrite(s"""echo "$config" >> /private/etc/apache2/other/proxy.conf""")
		general.Config.sudoWrite(s"""apachectl restart""")
	}
}