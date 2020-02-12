package won.bot.jobbot.util;

import java.math.RoundingMode;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import won.bot.jobbot.api.HokifyBotsApi;
import won.bot.jobbot.api.model.HokifyJob;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.vocabulary.SCHEMA;
import won.protocol.vocabulary.WONCON;
import won.protocol.vocabulary.WONMATCH;
import won.protocol.vocabulary.WXCHAT;
import won.protocol.vocabulary.WXHOLD;

public class JobAtomModelWrapper extends DefaultAtomModelWrapper {
    public JobAtomModelWrapper(String atomURI, HokifyJob job) {
        this(URI.create(atomURI), job);
    }

    public JobAtomModelWrapper(URI atomURI, HokifyJob job) {
        super(atomURI);
        Resource atom = this.getAtomModel().createResource(atomURI.toString());
        // Default Data & Information
        Resource seeksPart = atom.getModel().createResource();
        // @type
        atom.addProperty(RDF.type, SCHEMA.JOBPOSTING);
        // s:url
        atom.addProperty(SCHEMA.URL, "");
        // s:title
        atom.addProperty(SCHEMA.TITLE, job.getTitle());
        // s:datePosted
        // TODO:convert to s:Date (ISO 8601)
        atom.addProperty(SCHEMA.DATEPOSTED, job.getDate());
        // s:image
        Resource image = atom.getModel().createResource();
        image.addProperty(RDF.type, SCHEMA.URL);
        image.addProperty(SCHEMA.VALUE, job.getImage());
        atom.addProperty(SCHEMA.IMAGE, image);
        // s:hiringOrganization
        Resource hiringOrganisation = atom.getModel().createResource();
        hiringOrganisation.addProperty(RDF.type, SCHEMA.ORGANIZATION);
        hiringOrganisation.addProperty(SCHEMA.NAME, job.getCompany());
        atom.addProperty(SCHEMA.ORGANIZATION, hiringOrganisation);
        // s:jobLocation
        HashMap<String, String> location = HokifyBotsApi.fetchGeoLocation(job.getCity(), job.getCountry());
        if (location != null) {
            Resource jobLocation = atom.getModel().createResource();
            jobLocation.addProperty(RDF.type, SCHEMA.PLACE);
            // TODO look up lon/lat via nominatim
            atom.addProperty(SCHEMA.JOBLOCATION, jobLocation);
            DecimalFormat df = new DecimalFormat("##.######");
            df.setRoundingMode(RoundingMode.HALF_UP);
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
            String nwlat = df.format(Double.parseDouble(location.get("nwlat")));
            String nwlng = df.format(Double.parseDouble(location.get("nwlng")));
            String selat = df.format(Double.parseDouble(location.get("selat")));
            String selng = df.format(Double.parseDouble(location.get("selng")));
            String lat = df.format(Double.parseDouble(location.get("lat")));
            String lng = df.format(Double.parseDouble(location.get("lng")));
            String name = location.get("name");
            Resource boundingBoxResource = atom.getModel().createResource();
            Resource nwCornerResource = atom.getModel().createResource();
            Resource seCornerResource = atom.getModel().createResource();
            Resource geoResource = atom.getModel().createResource();
            jobLocation.addProperty(SCHEMA.NAME, name);
            jobLocation.addProperty(SCHEMA.GEO, geoResource);
            geoResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
            geoResource.addProperty(SCHEMA.LATITUDE, lat);
            geoResource.addProperty(SCHEMA.LONGITUDE, lng);
            RDFDatatype bigdata_geoSpatialDatatype = new BaseDatatype(
                    "http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon");
            geoResource.addProperty(WONCON.geoSpatial, lat + "#" + lng, bigdata_geoSpatialDatatype);
            jobLocation.addProperty(WONCON.boundingBox, boundingBoxResource);
            boundingBoxResource.addProperty(WONCON.northWestCorner, nwCornerResource);
            nwCornerResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
            nwCornerResource.addProperty(SCHEMA.LATITUDE, nwlat);
            nwCornerResource.addProperty(SCHEMA.LONGITUDE, nwlng);
            boundingBoxResource.addProperty(WONCON.southEastCorner, seCornerResource);
            seCornerResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
            seCornerResource.addProperty(SCHEMA.LATITUDE, selat);
            seCornerResource.addProperty(SCHEMA.LONGITUDE, selng);
        }
        // s:description
        atom.addProperty(SCHEMA.DESCRIPTION, filterDescriptionString(job.getDescription()));
        // s:baseSalary
        atom.addProperty(SCHEMA.BASESALARY, job.getSalary());
        // s:employmentType
        atom.addProperty(SCHEMA.EMPLYOMENTTYPE, job.getJobtype() != null ? job.getJobtype() : "");
        // s:industry
        for (Object field : job.getField()) {
            atom.addProperty(SCHEMA.INDUSTRY, parseField(field));
        }
        String[] tags = { "job", "hokify", "job offer" };
        for (String tag : tags) {
            atom.addProperty(WONCON.tag, tag);
        }
        seeksPart.addProperty(RDF.type, SCHEMA.PERSON);
        seeksPart.addProperty(WONMATCH.seeks, SCHEMA.JOBPOSTING);
        this.addSocket("#HoldableSocket", WXHOLD.HoldableSocketString);
        this.addSocket("#ChatSocket", WXCHAT.ChatSocketString);
        this.setDefaultSocket("#ChatSocket");
        this.addFlag(WONMATCH.NoHintForMe);
        atom.addProperty(WONMATCH.seeks, seeksPart);
    }

    private String filterDescriptionString(String description) {
        // TODO filter out contact information
        return description;
    }

    private String parseField(Object field) {
        // TODO parse the field string
        return field.toString();
    }
}
