package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.filter.PresentFilter;

/**
 * @author Endi S. Dewata
 */
public class ConflictingUIDFinderAction extends NISAction {

    public ConflictingUIDFinderAction() throws Exception {
        setName("Conflicting UID Finder");
        setDescription("Finds users from different domains with conflicting UIDs");
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        String federationName = nisFederationClient.getFederationClient().getName();
        String localPartitionName = request.getDomain();

        PartitionClient localPartitionClient = partitionManagerClient.getPartitionClient(federationName+"_"+localPartitionName);
        SourceClient localSourceClient = localPartitionClient.getSourceManagerClient().getSourceClient("LDAP");

        PartitionClient federationPartitionClient = nisFederationClient.getFederationClient().getPartitionClient();
        SourceClient nisSourceClient = federationPartitionClient.getSourceManagerClient().getSourceClient("NIS");

        PresentFilter localFilter = new PresentFilter("uidNumber");

        SearchRequest localRequest = new SearchRequest();
        localRequest.setFilter(localFilter);

        SearchResponse localResponse = new SearchResponse();

        localSourceClient.search(localRequest, localResponse);

        while (localResponse.hasNext()) {
            SearchResult localResult = localResponse.next();

            DN localDn = localResult.getDn();
            Attributes localAttributes = localResult.getAttributes();

            RDN localDomainRdn = localDn.get(2);
            Object localDomain = localDomainRdn.get("ou");

            Object localUid = localAttributes.getValue("uid");
            Object localUidNumber = localAttributes.getValue("uidNumber");

            if (debug) log.debug(localDn+": "+localUidNumber);

            SimpleFilter remoteFilter = new SimpleFilter("uidNumber", "=", localUidNumber);

            SearchRequest remoteRequest = new SearchRequest();
            remoteRequest.setFilter(remoteFilter);

            SearchResponse remoteResponse = new SearchResponse();

            nisSourceClient.search(remoteRequest, remoteResponse);

            while (remoteResponse.hasNext()) {
                SearchResult remoteResult = remoteResponse.next();

                DN remoteDn = remoteResult.getDn();

                if (localDn.equals(remoteDn)) continue;

                Attributes remoteAttributes = remoteResult.getAttributes();

                RDN remoteDomainRdn = remoteDn.get(2);
                Object remoteDomain = remoteDomainRdn.get("ou");

                Object remoteUid = remoteAttributes.getValue("uid");
                Object remoteUidNumber = remoteAttributes.getValue("uidNumber");

                if (debug) log.debug(" - "+remoteDn);

                Attributes attributes1 = new Attributes();
                attributes1.setValue("domain", localDomain);
                attributes1.setValue("uid", localUid);
                //attributes1.setValue("origUidNumber", origUidNumber1);
                attributes1.setValue("uidNumber", localUidNumber);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", remoteDomain);
                attributes2.setValue("uid", remoteUid);
                //attributes2.setValue("origUidNumber", origUidNumber2);
                attributes2.setValue("uidNumber", remoteUidNumber);

                response.add(new Conflict(attributes1, attributes2));
            }
        }

        response.close();
    }
}
