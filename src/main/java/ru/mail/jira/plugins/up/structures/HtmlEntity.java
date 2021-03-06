/*
 * Created by Andrey Markelov 11-11-2012. Copyright Mail.Ru Group 2012. All
 * rights reserved.
 */
package ru.mail.jira.plugins.up.structures;


import net.jcip.annotations.Immutable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Html JSON representation.
 * 
 * @author Andrey Markelov
 */
@Immutable
@XmlRootElement
public class HtmlEntity
{
    @XmlElement
    private String html;

    public HtmlEntity(String html)
    {
        this.html = html;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    @Override
    public String toString()
    {
        return ("HtmlEntity[html=" + html + "]");
    }
}
