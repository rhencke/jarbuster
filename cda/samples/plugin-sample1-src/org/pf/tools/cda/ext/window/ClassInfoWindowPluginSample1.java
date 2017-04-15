// ===========================================================================
// CONTENT  : CLASS ClassInfoWindowPluginSample1
// AUTHOR   : M.Duchrow
// VERSION  : 1.0 - 23/02/2007
// HISTORY  :
//  23/02/2007  mdu  CREATED
//
// Copyright (c) 2007, by M.Duchrow. All rights reserved.
// ===========================================================================
package org.pf.tools.cda.ext.window;

// ===========================================================================
// IMPORTS
// ===========================================================================
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.pf.tools.cda.ui.plugin.spi.ASubWindowPlugin;
import org.pfsw.odem.IContainer;
import org.pfsw.odem.IExplorableElement;
import org.pfsw.odem.IExplorableElementVisitor;
import org.pfsw.odem.IExplorationContext;
import org.pfsw.odem.IExplorationModelObject;
import org.pfsw.odem.INamespace;
import org.pfsw.odem.IType;

/**
 * Sample implementation of a plugin which can be invoked on any element
 * in the tree view.
 *
 * @author M.Duchrow
 * @version 1.0
 */
public class ClassInfoWindowPluginSample1 extends ASubWindowPlugin
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final String PLUGIN_PROVIDER = "Manfred Duchrow Consulting & Software";

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================
  private JTextArea textArea;

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with default values.
   */
  public ClassInfoWindowPluginSample1()
  {
    super();
  } // ClassInfoWindowPluginSample1() 

  // =========================================================================
  // PUBLIC INSTANCE METHODS
  // =========================================================================
  /**
   * Returns an action label for all explorable elements but not for an
   * IExplorableContext.
   */
  public String getActionText(Locale locale, IExplorationModelObject object)
  {
    if (object instanceof IExplorationContext)
    {
      return null;
    }
    if (object instanceof IExplorableElement)
    {
      IExplorableElement type = (IExplorableElement)object;
      return "Show details of " + type.getName();
    }
    return null;
  } // getActionText() 

  // -------------------------------------------------------------------------

  public String getPluginProvider()
  {
    return PLUGIN_PROVIDER;
  } // getPluginProvider() 

  // -------------------------------------------------------------------------

  public String getPluginVersion()
  {
    return "1.0.0";
  } // getPluginVersion() 

  // -------------------------------------------------------------------------

  /**
   * This method must be implemented to create the whole UI of the new window.
   * It returns a Frame containing all widgets that are necessary to show whatever
   * the plug-in wants to show.
   * 
   * @param controller The object that controls this plugin
   * @param modelObject The element on which this plug-in was invoked
   * @return A frame or null. If null is returned the plug-in execution get cancelled.
   */
  @SuppressWarnings("serial")
  public Frame createFrame(IExplorationModelObject modelObject)
  {
    JFrame frame;
    JPanel panel;
    JScrollPane scrollPane;
    JButton closeButton;
    Action closeAction;

    frame = new JFrame("Sample Plug-In 1 (" + modelObject.getName() + ")");
    frame.setSize(400, 300);

    panel = new JPanel(new GridLayout(2, 1));
    textArea = new JTextArea("The selected element is: " + modelObject.getName());
    scrollPane = new JScrollPane(textArea);
    panel.add(scrollPane);
    closeAction = new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
        close();
      }
    };
    closeButton = new JButton(closeAction);
    closeButton.setText("Close");
    panel.add(closeButton);
    frame.getContentPane().add(panel);
    if (modelObject instanceof INamespace)
    {
      this.listContainedTypes((INamespace)modelObject);
    }
    return frame;
  } // createFrame()

  // -------------------------------------------------------------------------

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================
  protected void listContainedTypes(INamespace aNamespace)
  {
    IExplorableElementVisitor visitor;
    final StringBuffer buffer = new StringBuffer(300);

    visitor = new IExplorableElementVisitor()
    {

      public boolean visitType(IType type)
      {
        buffer.append("  ");
        buffer.append(type.getName());
        buffer.append("\n");
        return true;
      }

      public boolean visitNamespace(INamespace namespace)
      {
        buffer.append(namespace.getName());
        buffer.append("\n");
        return true;
      }

      public boolean visitContainer(IContainer container)
      {
        // No containers under namespaces
        return true;
      }
    };
    aNamespace.accept(visitor);
    textArea.setText(buffer.toString());
  } // listContainedTypes()

  // -------------------------------------------------------------------------

} // class ClassInfoWindowPluginSample1 
