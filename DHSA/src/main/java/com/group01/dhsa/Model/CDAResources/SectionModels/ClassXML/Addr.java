package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

/**
 * The {@code Addr} class represents an address that can be serialized into XML.
 * This class contains various components of an address, including details such as
 * country, state, city, postal code, and street address line. It is used to map
 * address data in XML format.
 *
 * <p>This class is annotated with JAXB annotations for XML binding, enabling it to be used in FHIR-based systems
 * and other medical data exchange formats.</p>
 *
 * @see <a href="https://www.hl7.it/wp-content/uploads/2024/04/HL7IT-IG_CDA2_LDO-v1.2-S.pdf">Lettera di Dimissione Ospedaliera (CDA-LDO)</a>
 * <p>Section 3.1.12.1.1</p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Addr {

    /**
     * The use of the address (e.g., whether the address is for home, office, etc.).
     * This field is an XML attribute.
     */
    @XmlAttribute(name = "use")
    private String use;

    /**
     * The country of the address.
     * This field is an XML element.
     */
    @XmlElement(name = "country")
    private String country;

    /**
     * Optional field
     * The state or province of the address.
     * This field is an XML element.
     */
    @XmlElement(name = "state")
    private String state;

    /**
     * Optional field
     * The county or district of the address.
     * This field is an XML element.
     */
    @XmlElement(name = "county")
    private String county;

    /**
     * The city of the address.
     * This field is an XML element.
     */
    @XmlElement(name = "city")
    private String city;

    /**
     * Optional field
     * The census tract of the address, if available.
     * This field is an XML element.
     */
    @XmlElement(name = "censusTract")
    private String censusTract;

    /**
     * Optional field
     * The postal code of the address.
     * This field is an XML element.
     */
    @XmlElement(name = "postalCode")
    private String postalCode;

    /**
     * The street address line (e.g., street number and name).
     * This field is an XML element.
     */
    @XmlElement(name = "streetAddressLine")
    private String streetAddressLine;

    public Addr() {
    }

    public Addr(String use, String streetAddressLine, String country, String state, String city) {
        this.use = use;
        this.streetAddressLine = streetAddressLine;
        this.country = country;
        this.state = state;
        this.city = city;
    }

    /**
     * Returns the use of the address.
     *
     * @return the use of the address (e.g., home, office)
     */
    public String getUse() {
        return use;
    }

    /**
     * Sets the use of the address.
     *
     * @param use the use of the address (e.g., home, office)
     */
    public void setUse(String use) {
        this.use = use;
    }

    /**
     * Returns the country of the address.
     *
     * @return the country of the address
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country of the address.
     *
     * @param country the country of the address
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the state or province of the address.
     *
     * @return the state or province of the address
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state or province of the address.
     *
     * @param state the state or province of the address
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the county or district of the address.
     *
     * @return the county or district of the address
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the county or district of the address.
     *
     * @param county the county or district of the address
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * Returns the city of the address.
     *
     * @return the city of the address
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city of the address.
     *
     * @param city the city of the address
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the census tract of the address.
     *
     * @return the census tract of the address
     */
    public String getCensusTract() {
        return censusTract;
    }

    /**
     * Sets the census tract of the address.
     *
     * @param censusTract the census tract of the address
     */
    public void setCensusTract(String censusTract) {
        this.censusTract = censusTract;
    }

    /**
     * Returns the postal code of the address.
     *
     * @return the postal code of the address
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postal code of the address.
     *
     * @param postalCode the postal code of the address
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Returns the street address line (e.g., street number and name).
     *
     * @return the street address line
     */
    public String getStreetAddressLine() {
        return streetAddressLine;
    }

    /**
     * Sets the street address line (e.g., street number and name).
     *
     * @param streetAddressLine the street address line
     */
    public void setStreetAddressLine(String streetAddressLine) {
        this.streetAddressLine = streetAddressLine;
    }
}
