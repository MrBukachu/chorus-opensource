package com.infoclinika.mssharing.platform.model.test.request;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import com.infoclinika.mssharing.platform.model.testing.helper.Data;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.infoclinika.mssharing.platform.model.RequestsTemplate.OutboxItem;
import static org.testng.Assert.*;

public class GeneralRequestsTest extends AbstractTest {


    @Test
    public void testAppearanceOfLabCreationMessagesInOutbox() {
        final long kate = uc.createKateAndLab2();
        requestLab4and5CreationByKate();
        assertEquals(requests.getOutboxItems(kate).size(), 2);
    }

    @Test
    public void testProperNumberOfInboxMessages() {
        final long kate = uc.createKateAndLab2();
        final long request1 = requestLab4Creation();
        final long request2 = requestLab5Creation();
        requests.approve(admin(), getLabCreationRequest(request1));
        requests.approve(admin(), getLabCreationRequest(request2));
        assertEquals(requests.getInboxItems(kate).size(), 2);
    }

    @Test
    public void testMessagesAppearInSenderAndReceiver() {
        final long kate = uc.createKateAndLab2();
        final long request1 = requestLab4Creation();
        final long request2 = requestLab5Creation();
        assertEquals(requests.getInboxItems(admin()).size(), 2);
        requests.approve(admin(), getLabCreationRequest(request1));
        requests.approve(admin(), getLabCreationRequest(request2));
        assertEquals(requests.getInboxItems(kate).size(), 2);
    }

    @Test
    public void testDisplayInboxMessagesOfDifferentTypes() {
        final long kate = uc.createKateAndLab2();
        final long lab4Request = requestLab4Creation();
        final long bob = uc.createLab3AndBob();
        uc.addKateToLab3(); // +1 - approve membership
        createInstrumentAndApproveOperator(bob, kate);
        requests.refuse(admin(), getLabCreationRequest(lab4Request), "");
        assertEquals(requests.getInboxItems(kate).size(), 3);
    }

    @Test
    public void testDisplayOutboxMessagesOfDifferentTypes() {
        final long kate = uc.createKateAndLab2();
        final long bob = uc.createLab3AndBob();
        uc.addKateToLab3(); // +1 - approve membership
        createInstrumentAndApproveOperator(bob, kate);
        assertEquals(requests.getOutboxItems(kate).size(), 2);
    }


    @Test
    public void testMessagesDisappearFromInboxAfterApprove() {
        final long request1 = requestLab4Creation();
        final long request2 = requestLab5Creation();
        assertEquals(requests.getInboxItems(admin()).size(), 2); //request to lab 2 creation
        requests.approve(admin(), getLabCreationRequest(request1));
        requests.approve(admin(), getLabCreationRequest(request2));
        assertEquals(requests.getInboxItems(admin()).size(), 0);
    }

    @Test
    public void testMessagesDisappearFromInboxAfterRefuse() {
        final long request1 = requestLab4Creation();
        final long request2 = requestLab5Creation();
        assertEquals(requests.getInboxItems(admin()).size(), 2);
        requests.refuse(admin(), getLabCreationRequest(request1), "");
        requests.refuse(admin(), getLabCreationRequest(request2), "");
        assertEquals(requests.getInboxItems(admin()).size(), 0);
    }

    @Test
    public void testOutboxMessagesDisappearedAfterPressOk() {
        final long bob = uc.createLab3AndBob();
        final ImmutableSortedSet<OutboxItem> items = requests.getOutboxItems(bob);
        assertEquals(items.size(), 1);
        requests.removeOutboxItem(bob, items.first().id);
        assertEquals(requests.getOutboxItems(bob).size(), 0);
    }

    @Test
    public void testInboxMessagesDisappearedAfterPressOk() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        uc.addKateToLab3();
        createInstrumentAndApproveOperator(bob, kate);
        assertEquals(requests.getInboxItems(kate).size(), 2);
        requests.removeInboxItem(kate, requests.getInboxItems(kate).first().id);
        assertEquals(requests.getInboxItems(kate).size(), 1);
        requests.removeInboxItem(kate, requests.getInboxItems(kate).first().id);
        assertEquals(requests.getInboxItems(kate).size(), 0);
    }

    @Test
    public void testLabCreationOutboxMessagesDisappearedAfterPressOk() {
        final long paul = uc.createPaul();
        labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_KATE_INFO, "lab6"), Data.L_PAUL_INFO.email);
        labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_KATE_INFO, "lab7"), Data.L_PAUL_INFO.email);
        assertEquals(requests.getOutboxItems(paul).size(), 2);
        requests.removeOutboxItem(paul, requests.getOutboxItems(paul).first().id);
        assertEquals(requests.getOutboxItems(paul).size(), 1);
        requests.removeOutboxItem(paul, requests.getOutboxItems(paul).first().id);
        assertEquals(requests.getOutboxItems(paul).size(), 0);
    }

    @Test
    public void testAdminCanReadLabCreationDetails() {

        final long admin = admin();
        final Long labCreationId = labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_KATE_INFO, "lab6"), Data.L_PAUL_INFO.email);
        final DetailsReaderTemplate.LabItemTemplate requestDetails = requestsDetailsReader.readLabRequestDetails(admin, labCreationId);

        assertNotNull(requestDetails);
        assertEquals(requestDetails.contactEmail, Data.L_PAUL_INFO.email);
        assertEquals(requestDetails.headEmail, Data.L_KATE_INFO.email);
        assertEquals(requestDetails.headFirstName, Data.L_KATE_INFO.firstName);
        assertEquals(requestDetails.headLastName, Data.L_KATE_INFO.lastName);
        assertEquals(requestDetails.institutionUrl, Data.HARVARD_URL);
        assertEquals(requestDetails.name, "lab6");

    }

    @Test
    public void testHeadCanReadInstrumentCreationDetails() {

        final long labHead = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();

        final String name = "Instrument";
        final String serialNumber = "12341234";

        final Optional<Long> instrumentRequest =
                instrumentManagement.newInstrumentRequest(bob, uc.getLab3(), model, new InstrumentManagementTemplate.InstrumentDetailsTemplate(name, serialNumber, ""), new ArrayList<Long>());

        assertTrue(instrumentRequest.isPresent());

        final InstrumentCreationItemTemplate creationItem = requestsDetailsReader.readInstrumentCreation(labHead, instrumentRequest.get());

        assertNotNull(creationItem);
        assertEquals(creationItem.name, name);
        assertEquals(creationItem.labId, uc.getLab3().longValue());
        assertEquals(creationItem.model, model);
        assertEquals(creationItem.serialNumber, serialNumber);

    }

    @Test
    public void testInstrumentRequestRemovedFromInboxAfterAccept() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        uc.addKateToLab3();
        final long instrument1 = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final Long instrument2 = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.requestAccessToInstrument(kate, instrument1);
        instrumentManagement.requestAccessToInstrument(kate, instrument2);
        //1-lab membership, 2,3-approve instrument creation, 4,5-requested access to instrument from kate
        assertEquals(requests.getInboxItems(bob).size(), 5); //approve membership in lab 3
        requests.approve(bob, getInstrumentRequest(kate, instrument1));
        requests.refuse(bob, getInstrumentRequest(kate, instrument2), "");
        assertEquals(requests.getInboxItems(bob).size(), 3);
    }


    private void createInstrumentAndApproveOperator(long creator, long newOperator) {
        final long instrument = uc.createInstrumentAndApproveIfNeeded(creator, uc.getLab3()).get();
        instrumentManagement.requestAccessToInstrument(newOperator, instrument);
        requests.approve(creator, getInstrumentRequest(newOperator, instrument));
    }

    private void requestLab4and5CreationByKate() {
        requestLab4Creation();
        requestLab5Creation();
    }

    private String getInstrumentRequest(long kate, long instrument) {
        return "InstrumentStrategy" + String.valueOf(instrument) + "," + String.valueOf(kate);
    }

    private String getLabMembershipRequest(long request) {
        return "LabMembershipStrategy" + String.valueOf(request);
    }

    private String getLabCreationRequest(long request) {
        return "LabCreationStrategy" + String.valueOf(request);
    }

    private long requestLab5Creation() {
        return labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_KATE_INFO, "lab5"), Data.L_KATE_INFO.email);
    }

    private long requestLab4Creation() {
        return labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_KATE_INFO, "lab4"), Data.L_KATE_INFO.email);
    }

}
