/* CVS $Id: $ */
package org.oucs.gaboto.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
 
/**
 * Vocabulary definitions from ontologies/geo.owl 
 * @author Auto-generated by schemagen on 11 Mar 2009 08:56 
 */
public class GeoVocab {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.opengis.net/gml/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    
    public static final String exterior_URI = "http://www.opengis.net/gml/exterior";
    public static final ObjectProperty exterior = m_model.createObjectProperty( "http://www.opengis.net/gml/exterior" );
        
    
    
    public static final String featurename_URI = "http://www.opengis.net/gml/featurename";
    public static final DatatypeProperty featurename = m_model.createDatatypeProperty( "http://www.opengis.net/gml/featurename" );
        
    
    
    public static final String featuretypetag_URI = "http://www.opengis.net/gml/featuretypetag";
    public static final DatatypeProperty featuretypetag = m_model.createDatatypeProperty( "http://www.opengis.net/gml/featuretypetag" );
        
    
    
    public static final String lowerCorner_URI = "http://www.opengis.net/gml/lowerCorner";
    public static final DatatypeProperty lowerCorner = m_model.createDatatypeProperty( "http://www.opengis.net/gml/lowerCorner" );
        
    
    
    public static final String pos_URI = "http://www.opengis.net/gml/pos";
    public static final DatatypeProperty pos = m_model.createDatatypeProperty( "http://www.opengis.net/gml/pos" );
        
    
    
    public static final String posList_URI = "http://www.opengis.net/gml/posList";
    public static final DatatypeProperty posList = m_model.createDatatypeProperty( "http://www.opengis.net/gml/posList" );
        
    
    
    public static final String relationshiptag_URI = "http://www.opengis.net/gml/relationshiptag";
    public static final DatatypeProperty relationshiptag = m_model.createDatatypeProperty( "http://www.opengis.net/gml/relationshiptag" );
        
    
    
    public static final String upperCorner_URI = "http://www.opengis.net/gml/upperCorner";
    public static final DatatypeProperty upperCorner = m_model.createDatatypeProperty( "http://www.opengis.net/gml/upperCorner" );
        
    
    /** <p>From the W3C Geo vocabulary</p> */
    
    public static final String lat_URI = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
    public static final DatatypeProperty lat = m_model.createDatatypeProperty( "http://www.w3.org/2003/01/geo/wgs84_pos#lat" );
        
    
    /** <p>From the W3C Geo vocabulary</p> */
    
    public static final String long__URI = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
    public static final DatatypeProperty long_ = m_model.createDatatypeProperty( "http://www.w3.org/2003/01/geo/wgs84_pos#long" );
        
    
    
    public static final String Envelope_URI = "http://www.opengis.net/gml/Envelope";
    public static final OntClass Envelope = m_model.createClass( "http://www.opengis.net/gml/Envelope" );
        
    
    /** <p>The gml:posList property is defined for all elements of LineString</p> */
    
    public static final String LineString_URI = "http://www.opengis.net/gml/LineString";
    public static final OntClass LineString = m_model.createClass( "http://www.opengis.net/gml/LineString" );
        
    
    /** <p>The gml:posList property is defined for all elements of LinearRing</p> */
    
    public static final String LinearRing_URI = "http://www.opengis.net/gml/LinearRing";
    public static final OntClass LinearRing = m_model.createClass( "http://www.opengis.net/gml/LinearRing" );
        
    
    /** <p>The gml:pos property is defined for all elements of Point</p> */
    
    public static final String Point_URI = "http://www.opengis.net/gml/Point";
    public static final OntClass Point = m_model.createClass( "http://www.opengis.net/gml/Point" );
        
    
    
    public static final String Polygon_URI = "http://www.opengis.net/gml/Polygon";
    public static final OntClass Polygon = m_model.createClass( "http://www.opengis.net/gml/Polygon" );
        
    
    
    public static final String _Feature_URI = "http://www.opengis.net/gml/_Feature";
    public static final OntClass _Feature = m_model.createClass( "http://www.opengis.net/gml/_Feature" );
        
    
    
    public static final String _Geometry_URI = "http://www.opengis.net/gml/_Geometry";
    public static final OntClass _Geometry = m_model.createClass( "http://www.opengis.net/gml/_Geometry" );
        
    
}
