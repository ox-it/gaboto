/* $Id: $ */
package org.oucs.gaboto.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
 
/**
 * Vocabulary definitions from ontologies/DC.owl. 
 * 
 * @author Auto-generated by net.sf.gaboto.generation.VacabularyGenerator 
 */
public class DC {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    public static OntModel MODEL = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/dc/elements/1.1/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = MODEL.createResource( NS );
    
    // see VocabularyGenerator#writeObjectProperties()
    
    // see VocabularyGenerator#writeDatatypeProperties()
    
    // see VocabularyGenerator#writeAnnotationProperties()
    /** <p>An entity responsible for making contributions to the content of the resource.</p> */
    public static final 
    String contributor_URI = "http://purl.org/dc/elements/1.1/contributor";
    public static final 
    AnnotationProperty contributor = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/contributor" );
    
    /** <p>The extent or scope of the content of the resource.</p> */
    public static final 
    String coverage_URI = "http://purl.org/dc/elements/1.1/coverage";
    public static final 
    AnnotationProperty coverage = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/coverage" );
    
    /** <p>An entity primarily responsible for making the content of the resource.</p> */
    public static final 
    String creator_URI = "http://purl.org/dc/elements/1.1/creator";
    public static final 
    AnnotationProperty creator = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/creator" );
    
    /** <p>A date associated with an event in the life cycle of the resource.</p> */
    public static final 
    String date_URI = "http://purl.org/dc/elements/1.1/date";
    public static final 
    AnnotationProperty date = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/date" );
    
    /** <p>An account of the content of the resource.</p> */
    public static final 
    String description_URI = "http://purl.org/dc/elements/1.1/description";
    public static final 
    AnnotationProperty description = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/description" );
    
    /** <p>The physical or digital manifestation of the resource.</p> */
    public static final 
    String format_URI = "http://purl.org/dc/elements/1.1/format";
    public static final 
    AnnotationProperty format = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/format" );
    
    /** <p>An unambiguous reference to the resource within a given context.</p> */
    public static final 
    String identifier_URI = "http://purl.org/dc/elements/1.1/identifier";
    public static final 
    AnnotationProperty identifier = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/identifier" );
    
    /** <p>A language of the intellectual content of the resource.</p> */
    public static final 
    String language_URI = "http://purl.org/dc/elements/1.1/language";
    public static final 
    AnnotationProperty language = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/language" );
    
    /** <p>An entity responsible for making the resource available</p> */
    public static final 
    String publisher_URI = "http://purl.org/dc/elements/1.1/publisher";
    public static final 
    AnnotationProperty publisher = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/publisher" );
    
    /** <p>A reference to a related resource.</p> */
    public static final 
    String relation_URI = "http://purl.org/dc/elements/1.1/relation";
    public static final 
    AnnotationProperty relation = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/relation" );
    
    /** <p>Information about rights held in and over the resource.</p> */
    public static final 
    String rights_URI = "http://purl.org/dc/elements/1.1/rights";
    public static final 
    AnnotationProperty rights = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/rights" );
    
    /** <p>A reference to a resource from which the present resource is derived.</p> */
    public static final 
    String source_URI = "http://purl.org/dc/elements/1.1/source";
    public static final 
    AnnotationProperty source = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/source" );
    
    /** <p>The topic of the content of the resource.</p> */
    public static final 
    String subject_URI = "http://purl.org/dc/elements/1.1/subject";
    public static final 
    AnnotationProperty subject = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/subject" );
    
    /** <p>A name given to the resource.</p> */
    public static final 
    String title_URI = "http://purl.org/dc/elements/1.1/title";
    public static final 
    AnnotationProperty title = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/title" );
    
    /** <p>The nature or genre of the content of the resource.</p> */
    public static final 
    String type_URI = "http://purl.org/dc/elements/1.1/type";
    public static final 
    AnnotationProperty type = MODEL.createAnnotationProperty( "http://purl.org/dc/elements/1.1/type" );
    
    
    // see VocabularyGenerator#writeOntClasses()
    
    // see VocabularyGenerator#writeOntIndividuals()
    
}
