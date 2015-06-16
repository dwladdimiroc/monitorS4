package processElements;

import java.util.List;
import java.util.Map;	
import java.util.ArrayList;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;

import lexiconCreate.EarthQuakeLexEvaluator;
import lexiconCreate.LexiconEvaluator;

import cmu.arktweetnlp.Twokenize;
import eda.Tweet;

public class SentimientoMalletPE extends ProcessingElement {
	private static Logger logger = LoggerFactory
			.getLogger(SentimientoMalletPE.class);

	private boolean showEvent = false;
	
	EventFactory eventFactory;
	Stream<Event> downStream;

    boolean seen = false;

    private EarthQuakeLexEvaluator eqLex;
    private LexiconEvaluator elhPol;
    
    private List<String> tokens;
    private Map<String,Object> features;
    
    //evaluateEarthQuakeLex
    double earthPos;
    double earthNeg;
    double earthSub;
    double earthNeu;
    
    //evaluateElhPolar
    int elhPos;
    int elhNeg;
    
    //evaluateSentimentLinearModel
    double subScore;
    double polScore;
    int label;

    // weights for the Subjectivity Logistic Regression
    double InterWsub=-3.056370435;
    double earthSubWsub=0.007649282;
    double earthNeuWsub=0.157909987;
    double earthPosWsub=0.096099674;
    double earthNegWsub= -0.040263249;
    double elhPosWsub=0.295295301;
    double elhNegWsub=1.525096124;


    // weights for the Polarity Logistic Regression
    double InterceptWpol=1.16095248;
    double earthPosWpol=0.08285113;
    double earthNegWpol=0.08058357;
    double elhPosWpol=0.52087827;
    double elhNegWpol=-2.30640353;
    
    Twokenize tokenizer;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {

    	if(!seen){
    		tokenizer = new Twokenize();
    		seen = true;
    	}
		
		Tweet tweet = event.get("tweet", Tweet.class);
		if(showEvent){logger.debug(tweet.toString());}


    	tokenize(tweet.getText());
    	evaluateEarthQuakeLex(eqLex);
    	evaluateElhPolar(elhPol);
    	evaluateSentimentLinearModel();

        Tweet newTweet = tweet.getClone();


    	 
        if( -15 < (earthPos+earthNeg)  && (earthPos+earthNeg) < 15){
            earthNeg = earthNeg * -1;
            if(earthPos > earthNeg){
                newTweet.addHashMap("sentimientoC","positivo");
            }else{
                newTweet.addHashMap("sentimientoC","negativo");
            }

        }else{
            newTweet.addHashMap("sentimientoC","null");
        }


		 //System.out.println("elhPos: " + elhPos + " | elhNeg: " + elhNeg);
		 //System.out.println("subScore: " + subScore + " | polScore: " + polScore + " | label: " + label);
         
		Event eventOutput = eventFactory.newEvent(newTweet);
		
		eventOutput.put("levelTweet", Integer.class, getEventCount() % configuration.getReplication());
		downStream.put(eventOutput);
		
		//event.put("levelMallet", Integer.class, 1);
		//downStream.put(event);
	}

    public void evaluateElhPolar(LexiconEvaluator lex){

        elhPos=0;
        elhNeg=0;

        // Adds the scores of the words according to the Lexicon
        for(String word:tokens){
                if(lex.getDict().containsKey(word)){
                        String polarity=lex.getDict().get(word);
                        if(polarity.equals("positive"))
                                elhPos++;
                        else
                                elhNeg++;

                }                        
        }

    }//evaluateElhPolar

    // Evaluates the linear classifer
    public void evaluateSentimentLinearModel(){
    	
		subScore = 0;
		polScore = 0;
		label = 0;

        subScore=InterWsub+earthSubWsub*earthSub+earthNeuWsub*earthNeu+earthPosWsub*earthPos
                        +earthNegWsub*earthNeg+elhPosWsub*elhPos+elhNegWsub*elhNeg;

        polScore=InterceptWpol+earthPosWpol*earthPos+earthNegWpol*earthNeg+
                        elhPosWpol*elhPos+elhNegWpol*elhNeg;

        
        // Label corresponds to the polarity class, 1 for positive, 0 for neutral and -1 for negative
        if(subScore<=0){
                label=0;
        }
        else{
                if(polScore<0){
                        label=-1;
                }
                else{
                        label=1;
                }
        }


    }


    public void evaluateEarthQuakeLex(EarthQuakeLexEvaluator eqLex){

        earthPos=0;
        earthNeg=0;
        earthSub=0;
        earthNeu=0;

        // Adds the scores of the words according to the Lexicon
        for(String word:tokens){
                if(eqLex.getDict().containsKey(word)){
                        Map<String,Double> emoMap=eqLex.getWord(word);

                        double polarity=emoMap.get("pol");
                        if(polarity>0){
                                earthPos += polarity;                                         
                        }
                        else if(polarity<0){
                                earthNeg += polarity;
                        }

                        double subjectivity=emoMap.get("subj");

                        if(subjectivity>0){
                                earthSub += emoMap.get("subj");                                                                
                        }
                        else if(subjectivity<0)
                                earthNeu += emoMap.get("subj");
                }
        }

    }//evaluateEarthQuakeLex

    public void tokenize(String tweet){
    	 
        String content=tweet.toLowerCase();
        content=content.replaceAll("([aeiou])\\1+","$1"); // remove repeated vowels
               

        tokens=new ArrayList<String>();

        for(String word:tokenizer.tokenizeRawTweetText(content)){

                String cleanWord=word;


                // Replace URLs for a special token URL
                if(word.matches("http.*|www\\..*")){
                        cleanWord="URL";
                }
                
                // Replace user mentions to a special token USER
                else if(word.matches("@.*")){
                        cleanWord="USER";
                }        

                tokens.add(cleanWord);
                
        }


    }//tokenize
    

	@Override
	protected void onCreate() {
		logger.info("Create Sentiment FB PE");
		eventFactory = new EventFactory();

		eqLex=new EarthQuakeLexEvaluator("./kudaApp/extra/earthQuakeLex.csv");
        eqLex.processDict();

        elhPol = new LexiconEvaluator("./kudaApp/extra/earthQuakeLex.csv");
        elhPol.processDict();
	}

	@Override
	protected void onRemove() {

	}

}
