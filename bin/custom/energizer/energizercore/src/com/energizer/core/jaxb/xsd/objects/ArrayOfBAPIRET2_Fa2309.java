//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.20 at 04:53:49 PM IST 
//


package com.energizer.core.jaxb.xsd.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfBAPIRET2_fa2309 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfBAPIRET2_fa2309">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BAPIRET2" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}BAPIRET2_fa2309" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfBAPIRET2_fa2309", propOrder = {
    "bapiret2"
})
public class ArrayOfBAPIRET2_Fa2309 {

    @XmlElement(name = "BAPIRET2")
    protected List<BAPIRET2_Fa2309> bapiret2;

    /**
     * Gets the value of the bapiret2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bapiret2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBAPIRET2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BAPIRET2_Fa2309 }
     * 
     * 
     */
    public List<BAPIRET2_Fa2309> getBAPIRET2() {
        if (bapiret2 == null) {
            bapiret2 = new ArrayList<BAPIRET2_Fa2309>();
        }
        return this.bapiret2;
    }

}
