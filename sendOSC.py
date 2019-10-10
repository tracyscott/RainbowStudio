# This Python file uses the following encoding: utf-8
import sys
import OSC

OSC_ADDRESS = "127.0.0.1"
OSC_PORT = 7979

def main():
  client = init_osc(OSC_ADDRESS, OSC_PORT)

  if len(sys.argv) == 1:
    print "Type messages to send:\n"

    try:
      for line in iter(sys.stdin.readline, b''):
        try:
          client.send(osc_msg("/rainbow/textupdate", line[:-1]))
        except Exception,e:
          print "❌", e
    except KeyboardInterrupt:
      sys.stdout.flush()
    pass
  else:
    message = sys.argv[1]
    client.send(osc_msg("/rainbow/textupdate", message))

def init_osc(ip_address, port):
  c = OSC.OSCClient()
  c.connect((ip_address, port))
  print "OSC connection open to %s:%d" % (ip_address, port)

  return c

def osc_msg(osc_address, message):
  oscmsg = OSC.OSCMessage()
  oscmsg.setAddress(osc_address)
  oscmsg.append(message)
  return oscmsg

if __name__ == "__main__":
  main()
