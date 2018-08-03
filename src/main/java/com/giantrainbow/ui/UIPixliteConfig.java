package com.giantrainbow.ui;

import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UITextBox;

public class UIPixliteConfig extends UICollapsibleSection {
  public static StringParameter pixlite1IpP = new StringParameter("Pixlite 1 IP", "192.168.2.134");
  public static StringParameter pixlite1PortP = new StringParameter("Pixlite 1 Port", "6454");
  public static StringParameter pixlite2IpP = new StringParameter("Pixlite 2 IP", "192.168.2.134");
  public static StringParameter pixlite2PortP = new StringParameter("Pixlite 2 Port", "6455");

  public static UITextBox pixlite1Ip;
  public static UITextBox pixlite1Port;
  public static UITextBox pixlite2Ip;
  public static UITextBox pixlite2Port;

  public UIPixliteConfig(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    setTitle("Pixlite Config");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    pixlite1Ip = new UITextBox(0,0, contentWidth - 10, 20 );
    pixlite1Ip.setParameter(pixlite1IpP);
    pixlite1Ip.addToContainer(this);
    pixlite1Port = new UITextBox(0, 0, contentWidth - 10, 20);
    pixlite1Port.setParameter(pixlite1PortP);
    pixlite1Port.addToContainer(this);
    pixlite2Ip = new UITextBox(0, 0, contentWidth - 10, 20);
    pixlite2Ip.setParameter(pixlite2IpP);
    pixlite2Ip.addToContainer(this);
    pixlite2Port = new UITextBox(0, 0, contentWidth - 10, 20);
    pixlite2Port.setParameter(pixlite2PortP);
    pixlite2Port.addToContainer(this);
  }
}
