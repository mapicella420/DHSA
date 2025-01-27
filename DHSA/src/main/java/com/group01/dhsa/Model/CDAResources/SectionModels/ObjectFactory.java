package com.group01.dhsa.Model.CDAResources.SectionModels;

import com.group01.dhsa.Model.CDAResources.ClinicalDocument;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.annotation.*;

@XmlRegistry
public class ObjectFactory {

    // Costruttore di default
    public ObjectFactory() {}

    // Metodo per creare un'istanza di ClinicalDocument
    public ClinicalDocument createClinicalDocument(Integer idNumber) {
        return new ClinicalDocument(idNumber);
    }

    // Metodo per creare un'istanza di TypeId
    public TypeId createTypeId() {
        return new TypeId();
    }

    // Metodo per creare un'istanza di TemplateId
    public TemplateId createTemplateId() {
        return new TemplateId();
    }

    // Metodo per creare un'istanza di Id
    public Id createId() {
        return new Id();
    }

    // Metodo per creare un'istanza di RealmCode
    public RealmCode createRealmCode() {
        return new RealmCode();
    }

    // Metodo per creare un'istanza di Code
    public Code createCode() {
        return new Code();
    }

    // Metodo per creare un'istanza di Title
    public Title createTitle() {
        return new Title();
    }

    // Metodo per creare un'istanza di EffectiveTime
    public EffectiveTime createEffectiveTime() {
        return new EffectiveTime();
    }

    // Metodo per creare un'istanza di ConfidentialityCode
    public ConfidentialityCode createConfidentialityCode() {
        return new ConfidentialityCode();
    }

    // Metodo per creare un'istanza di LanguageCode
    public LanguageCode createLanguageCode() {
        return new LanguageCode();
    }

    // Metodo per creare un'istanza di SetId
    public SetId createSetId() {
        return new SetId();
    }

    // Metodo per creare un'istanza di VersionNumber
    public VersionNumber createVersionNumber() {
        return new VersionNumber();
    }

    // Metodo per creare un'istanza di PatientCDA
    public PatientCDA createPatient() {
        return new PatientCDA();
    }

    // Metodo per creare un'istanza di ObservationCDA
    public ObservationCDA createObservation() {
        return new ObservationCDA();
    }

    public Addr createAddr() {
        return new Addr();
    }

    public AdministrativeGenderCode createAdministrativeGenderCode() {
        return new AdministrativeGenderCode();
    }

    public BirthTime createBirthTime() {
        return new BirthTime();
    }

    public Name createName() {
        return new Name();
    }

    public RecordTarget createRecordTarget() {
        return new RecordTarget();
    }

    public Author createAuthor() {
        return new Author();
    }

    public AssignedAuthor createAssignedAuthor() {
        return new AssignedAuthor();
    }

    public AssignedPerson createAssignedPerson() {
        return new AssignedPerson();
    }

    public Custodian createCustodian() {
        return new Custodian();
    }

    public AssignedCustodian createAssignedCustodian() { return new AssignedCustodian(); }

    public RepresentedCustodianOrganization createRepresentedCustodianOrganization() { return new RepresentedCustodianOrganization(); }

    public AssignedEntity createAssignedEntity() { return new AssignedEntity(); }

    public LegalAuthenticator createLegalAuthenticator() { return new LegalAuthenticator(); }

    public RepresentedOrganization createRepresentedOrganization() { return new RepresentedOrganization(); }

    public Telecom createTelecom() {return new Telecom(); }

    public SignatureCode createSignatureCode() { return new SignatureCode(); }

    public AsOrganizationPartOf createAsOrganizationPartOf() { return new AsOrganizationPartOf(); }

    public ComponentOf createComponentOf() { return new ComponentOf(); }

    public EncompassingEncounter createEncompassingEncounter() { return new EncompassingEncounter(); }

    public HealthCareFacility createHealthCareFacility() { return new HealthCareFacility(); }

    public High createHigh() {return new High(); }

    public Location createLocation() {return new Location(); }

    public LocationHealth createLocationHealth() { return new LocationHealth(); }

    public Low createLow() {return new Low(); }

    public ResponsibleParty createResponsibleParty() { return new ResponsibleParty(); }

    public ServiceProviderOrganization createServiceProviderOrganization() { return new ServiceProviderOrganization(); }

    public ObservationCDA createObservationCDA() { return new ObservationCDA(); }

    public Component createComponent() { return new Component(); }

    public ComponentInner createComponentInner() { return new ComponentInner(); }

    public Entry createEntry() { return new Entry(); }

    public EntryRelationship createEntryRelationship() { return new EntryRelationship(); }

    public Section createSection(){ return new Section();}

    public StructuredBody createStructuredBody(){ return new StructuredBody();}

    public Act createAct() { return new Act(); }

    public Text createText() { return new Text(); }

    public Value createValue() { return new Value(); }

    public Translation createTranslation() { return new Translation(); }

    public Paragraph createParagraph() { return new Paragraph(); }

    public StructuredList createStructuredList() { return new StructuredList(); }

    public Table createTable() { return new Table(); }

    public TableCell createTableCell() { return new TableCell(); }

    public TableRow createTableRow() { return new TableRow(); }

    public ListItem createListItem() { return new ListItem(); }

    public StatusCode createStatusCode() { return new StatusCode(); }

    public Time createTime() { return new Time(); }

    public ValueContent createValueContent() { return new ValueContent(); }

    public TableHead createTableHead() { return new TableHead(); }

    public TableBody createTableBody() { return new TableBody(); }

    public ProcedureCDA createProcedure() { return new ProcedureCDA(); }

}