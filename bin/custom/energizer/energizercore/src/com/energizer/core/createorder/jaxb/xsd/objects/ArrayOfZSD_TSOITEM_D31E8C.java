//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.20 at 05:57:26 PM IST 
//


package com.energizer.core.createorder.jaxb.xsd.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfZSD_TSOITEM_d31e8c complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfZSD_TSOITEM_d31e8c">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ZSD_TSOITEM" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ZSD_TSOITEM_d31e8c" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfZSD_TSOITEM_d31e8c", propOrder = {
    "zsdTSOITEM"
})
public class ArrayOfZSD_TSOITEM_D31E8C {

    @XmlElement(name = "ZSD_TSOITEM")
    protected List<ZSD_TSOITEM_D31E8C> zsdTSOITEM;

    /**
     * Gets the value of the zsdTSOITEM property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the zsdTSOITEM property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getZSD_TSOITEM().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ZSD_TSOITEM_D31E8C }
     * 
     * 
     */
    public List<ZSD_TSOITEM_D31E8C> getZSD_TSOITEM() {
        if (zsdTSOITEM == null) {
            zsdTSOITEM = new ArrayList<ZSD_TSOITEM_D31E8C>();
        }
        return this.zsdTSOITEM;
    }

}
