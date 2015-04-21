//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.20 at 05:57:26 PM IST 
//


package com.energizer.core.createorder.jaxb.xsd.objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for BAPIRET2_d31e8c complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BAPIRET2_d31e8c">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TYPE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ID" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="NUMBER" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;totalDigits value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MESSAGE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="220"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LOG_NO" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LOG_MSG_NO" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;totalDigits value="6"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MESSAGE_V1" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MESSAGE_V2" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MESSAGE_V3" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MESSAGE_V4" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PARAMETER" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="32"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ROW" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="FIELD" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="30"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SYSTEM" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="10"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BAPIRET2_d31e8c", propOrder =
{ "type", "id", "number", "message", "logNO", "logMSGNO", "messageV1", "messageV2", "messageV3", "messageV4", "parameter", "row",
		"field", "system" })
public class BAPIRET2_D31E8C
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "type=" + type.getValue() + ", id=" + id.getValue() + ", number=" + number.getValue() + ", message="
				+ message.getValue() + ", <br/> logNO=" + logNO.getValue() + ", logMSGNO=" + logMSGNO.getValue() + ", messageV1="
				+ messageV1.getValue() + ", messageV2=" + messageV2.getValue() + ", <br/> messageV3=" + messageV3.getValue()
				+ ", messageV4=" + messageV4.getValue() + ", parameter=" + parameter.getValue() + ", row=" + row.getValue()
				+ ", <br/> field=" + field.getValue() + ", system=" + system.getValue();
	}

	@XmlElementRef(name = "TYPE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> type;
	@XmlElementRef(name = "ID", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> id;
	@XmlElementRef(name = "NUMBER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<Integer> number;
	@XmlElementRef(name = "MESSAGE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> message;
	@XmlElementRef(name = "LOG_NO", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> logNO;
	@XmlElementRef(name = "LOG_MSG_NO", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<Integer> logMSGNO;
	@XmlElementRef(name = "MESSAGE_V1", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> messageV1;
	@XmlElementRef(name = "MESSAGE_V2", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> messageV2;
	@XmlElementRef(name = "MESSAGE_V3", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> messageV3;
	@XmlElementRef(name = "MESSAGE_V4", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> messageV4;
	@XmlElementRef(name = "PARAMETER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> parameter;
	@XmlElementRef(name = "ROW", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<Integer> row;
	@XmlElementRef(name = "FIELD", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> field;
	@XmlElementRef(name = "SYSTEM", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> system;

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getTYPE()
	{
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setTYPE(final JAXBElement<String> value)
	{
		this.type = value;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getID()
	{
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setID(final JAXBElement<String> value)
	{
		this.id = value;
	}

	/**
	 * Gets the value of the number property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link Integer }{@code >}
	 * 
	 */
	public JAXBElement<Integer> getNUMBER()
	{
		return number;
	}

	/**
	 * Sets the value of the number property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link Integer }{@code >}
	 * 
	 */
	public void setNUMBER(final JAXBElement<Integer> value)
	{
		this.number = value;
	}

	/**
	 * Gets the value of the message property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getMESSAGE()
	{
		return message;
	}

	/**
	 * Sets the value of the message property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setMESSAGE(final JAXBElement<String> value)
	{
		this.message = value;
	}

	/**
	 * Gets the value of the log_NO property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getLOG_NO()
	{
		return logNO;
	}

	/**
	 * Sets the value of the log_NO property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setLOG_NO(final JAXBElement<String> value)
	{
		this.logNO = value;
	}

	/**
	 * Gets the value of the log_MSG_NO property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link Integer }{@code >}
	 * 
	 */
	public JAXBElement<Integer> getLOG_MSG_NO()
	{
		return logMSGNO;
	}

	/**
	 * Sets the value of the log_MSG_NO property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link Integer }{@code >}
	 * 
	 */
	public void setLOG_MSG_NO(final JAXBElement<Integer> value)
	{
		this.logMSGNO = value;
	}

	/**
	 * Gets the value of the message_V1 property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getMESSAGE_V1()
	{
		return messageV1;
	}

	/**
	 * Sets the value of the message_V1 property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setMESSAGE_V1(final JAXBElement<String> value)
	{
		this.messageV1 = value;
	}

	/**
	 * Gets the value of the message_V2 property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getMESSAGE_V2()
	{
		return messageV2;
	}

	/**
	 * Sets the value of the message_V2 property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setMESSAGE_V2(final JAXBElement<String> value)
	{
		this.messageV2 = value;
	}

	/**
	 * Gets the value of the message_V3 property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getMESSAGE_V3()
	{
		return messageV3;
	}

	/**
	 * Sets the value of the message_V3 property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setMESSAGE_V3(final JAXBElement<String> value)
	{
		this.messageV3 = value;
	}

	/**
	 * Gets the value of the message_V4 property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getMESSAGE_V4()
	{
		return messageV4;
	}

	/**
	 * Sets the value of the message_V4 property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setMESSAGE_V4(final JAXBElement<String> value)
	{
		this.messageV4 = value;
	}

	/**
	 * Gets the value of the parameter property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getPARAMETER()
	{
		return parameter;
	}

	/**
	 * Sets the value of the parameter property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setPARAMETER(final JAXBElement<String> value)
	{
		this.parameter = value;
	}

	/**
	 * Gets the value of the row property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link Integer }{@code >}
	 * 
	 */
	public JAXBElement<Integer> getROW()
	{
		return row;
	}

	/**
	 * Sets the value of the row property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link Integer }{@code >}
	 * 
	 */
	public void setROW(final JAXBElement<Integer> value)
	{
		this.row = value;
	}

	/**
	 * Gets the value of the field property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getFIELD()
	{
		return field;
	}

	/**
	 * Sets the value of the field property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setFIELD(final JAXBElement<String> value)
	{
		this.field = value;
	}

	/**
	 * Gets the value of the system property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public JAXBElement<String> getSYSTEM()
	{
		return system;
	}

	/**
	 * Sets the value of the system property.
	 * 
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setSYSTEM(final JAXBElement<String> value)
	{
		this.system = value;
	}

}
