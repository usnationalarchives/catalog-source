import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.store.*;
import com.searchtechnologies.qpl.core.*;
import com.searchtechnologies.qpl.*;
import org.apache.lucene.queries.BoostingQuery;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.automaton.CompiledAutomaton;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.document.DateTools;
import java.text.SimpleDateFormat;
import org.apache.solr.schema.TrieDateField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.schema.IndexSchema;

/************************************************************************/
/*** UTILITY FUNCTIONS FOR CONVERTING NOT QUERIES TO STANDARD QUERIES ***/
/************************************************************************/

/** Convert MUST_NOT clauses to a real BooleanQuery. ***/
Query convertNotToQuery(clause) {
  def realNotQuery = new BooleanQuery();
  realNotQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
  realNotQuery.add(clause);
  return realNotQuery;
}

/*** Checks to see if the operand is a BooleanClause and coverts it to a query if so.
 * Otherwise, just returns the operand.
 */
Query toQuery(operand) {
  if(operand instanceof Query) return operand;
  else if(operand instanceof BooleanClause) {
    if(operand.getOccur() == BooleanClause.Occur.MUST_NOT) {
      return convertNotToQuery(operand);
    }
    else
      throw new Exception("Internal error: Found BooleanClause for MUST or SHOULD where only MUST_NOT was expected. (" + operand.toString() + ")");
  }
  else
    throw new Exception("Internal error: Found something other than Query or BooleanClause for an operand. (" + operand.toString() + ")");
}


builder.finalize = {ctx, qplRoot, engineRoot -> 
  return toQuery(engineRoot);
}

builder.before = {ctx, op, operands ->
  if (op.getWindow() >= 0) {
    def termsList = [];
    operands.each{
      if (it instanceof SpanQuery) {
      	termsList.add(it)
      }
      else { 
        println "Unsupported operand in Operator before: " + it
        //ignore operator other than termquery and wildcardquery
      }
    }
    
    if(termsList.size() > 0) {
      SpanQuery[] spanQ = termsList.toArray(new SpanQuery[termsList.size()]);
      Query query = new SpanNearQuery(spanQ, op.getWindow(), true);
      return query;
    }
  } else {
    BooleanQuery bq = new BooleanQuery();
    operands.each {
      if(it instanceof BooleanClause)
        bq.add(it);
      else
       bq.add(it, BooleanClause.Occur.MUST);
    };
    if(op.hasBoost()) bq.setBoost(op.boost);
    return bq;
  }
};

builder.between = {ctx, op, operands ->
  def termsList = [];
  operands.each{
    if (it instanceof SpanQuery) {
      termsList.add(it)
    } else { 
        println "Unsupported operand in Operator between: " + it
    }
  }

  if(termsList.size() == 3) {
	SpanQuery[] spanAry = new SpanQuery[3];
	spanAry[0] = termsList.getAt(0);
	spanAry[1] = termsList.getAt(2);
	spanAry[2] = termsList.getAt(1);

    Query query = new SpanNearQuery(spanAry, op.getWindow(), true);
    if(op.hasBoost()) query.setBoost(op.boost);
    return query;
  } else if (termsList.size() == 4) {
	SpanQuery[] spanAry = new SpanQuery[3];
	spanAry[0] = termsList.getAt(0);
	spanAry[1] = termsList.getAt(2);
	spanAry[2] = termsList.getAt(1);
    Query positiveQuery = new SpanNearQuery(spanAry, op.getWindow(), true);
	SpanQuery negativeQuery = termsList.getAt(3);
	
	Query query = new SpanNotQuery(positiveQuery, negativeQuery);
    if(op.hasBoost()) query.setBoost(op.boost);
    return query;
  } else {
    println "Incorrect number of operands in Operator between: " + termsList.size()
  }
}

builder.near = {ctx, op, operands ->
  if (op.getWindow() >= 0) {
    def termsList = [];
    operands.each{
      if (it instanceof SpanQuery) {
      	termsList.add(it)
      }
      else { 
        println "Unsupported operand in Operator before: " + it
        //ignore operator other than termquery and wildcardquery
      }
    }
    
    if(termsList.size() > 0) {
      SpanQuery[] spanQ = termsList.toArray(new SpanQuery[termsList.size()]);
      Query query = new SpanNearQuery(spanQ, op.getWindow(), false);
      if(op.hasBoost()) query.setBoost(op.boost);
      return query;
    }
  } else {
    BooleanQuery bq = new BooleanQuery();
    operands.each {
      if(it instanceof BooleanClause)
        bq.add(it);
      else
        bq.add(it, BooleanClause.Occur.MUST);
    };
    if(op.hasBoost()) bq.setBoost(op.boost);
    return bq;
  }
};

/*******************************************/
/*** OPERATOR BASICS and(), or(), term() ***/
/*******************************************/

builder.and = {ctx, op, operands ->
  if (op.getWindow() >= 0) {
    def termsList = [];
    operands.each{
      if (it instanceof SpanQuery) {
      	termsList.add(it)
      }
      else { 
        println "Unsupported operand in Operator AND(with window): " + it
      }
    }
    
    if(termsList.size() > 0) {
      SpanQuery[] spanQ = termsList.toArray(new SpanQuery[termsList.size()]);
      Query query = new SpanNearQuery(spanQ, op.getWindow(), false);
      if(op.hasBoost()) query.setBoost(op.boost);
      return query;
    }
  } else {
    BooleanQuery bq = new BooleanQuery();
    operands.each {
      if(it instanceof BooleanClause)
        bq.add(it);
      else
        bq.add(it, BooleanClause.Occur.MUST);
    };
    if(op.hasBoost()) bq.setBoost(op.boost);
    return bq;
  }
};


builder.or = {ctx, op, operands ->
  def orOp
  if (ctx.window >= 0) {
    orOp = new SpanOrQuery()
    operands.each{ orOp.addClause(toQuery(it)) };
  } else {
    orOp = new BooleanQuery();
    operands.each{ orOp.add(toQuery(it), BooleanClause.Occur.SHOULD) };
  }
  
  if(op.hasBoost()) orOp.setBoost(op.boost);
  return orOp;
};

builder.term = {ctx, op, operands -> 
  def termOp
  if(ctx.window >= 0) {
  	def termVal = op.getTerm()
    if (termVal.contains("*") || termVal.contains("?")) {
      def wildcardOp = new WildcardQuery(new Term(ctx.fieldOrDefault, op.term))
      termOp = new SpanMultiTermQueryWrapper<WildcardQuery>(wildcardOp);
    } else {
      termOp = new SpanTermQuery(new Term(ctx.fieldOrDefault, op.term));
    }
  } else {
    if (op.term.equals("*") && ctx.fieldOrDefault.equals("*")) {
      //"*:* all docs"
      termOp = new MatchAllDocsQuery()
    } else if (op.term.contains("*") || op.term.contains("?")) {
  	  wTerm = new Term(ctx.fieldOrDefault, op.term)
  	  termOp = new WildcardQuery(wTerm)
    } else {
      termOp = new TermQuery(new Term(ctx.fieldOrDefault, op.term))
    }
  }
  if(op.hasBoost()) termOp.setBoost(op.boost);
  return termOp;
};

builder.wildcard = {ctx, op, operands -> 
  def wildcardOp
  if (op.term.equals("*") && ctx.fieldOrDefault.equals("*")) {
    //"*:* all docs"
    wildcardOp = new MatchAllDocsQuery()
  } else {
  	if(ctx.window >= 0) {
	  wcOp = new WildcardQuery(new Term(ctx.fieldOrDefault, op.term))
      wildcardOp = new SpanMultiTermQueryWrapper<WildcardQuery>(wcOp);
  	} else {
      wildcardOp = new WildcardQuery(new Term(ctx.fieldOrDefault, op.term))
    }
  }
  
  if(op.hasBoost()) wildcardOp.setBoost(op.boost);
  return wildcardOp;
};

/****************************/
/*** HANDLING NOT QUERIES ***/
/****************************/

/**
 * not() is complicated because not() cannot stand alone. It must be bubbled up to a parent BooleanQuery in Lucene.
 * 
 * This is solved below by having not() return a BooleanClause instead of a Query object. This means that all 
 * other methods which might expect a Query object must now also look for BooleanClause and handle it appropriately. 
 */

builder.not = {ctx, op, operands ->
  def notOperand = null;
  if(operands.size() > 1) {
    notOperand = new BooleanQuery();
    operands.each{ notOperand.add(toQuery(it), BooleanClause.Occur.SHOULD) };
  }
  else if(operands.size() == 1) {
    notOperand = operands.get(0);
  }
  else {
    throw new Exception("Illegal not() operator with zero operands");
  }
  
  if(notOperand instanceof BooleanClause) {
    // convert not(not(x)) --> x
    if(notOperand.getOccur() == BooleanClause.Occur.MUST_NOT)
      return notOperand.getQuery();
    else
      throw new Exception("Illegal BooleanClause found inside not() operator (" + notOperand.getOccur() + ")");
  }

  return new BooleanClause(notOperand, BooleanClause.Occur.MUST_NOT);
};


/***********************************/
/*** RELEVANCY RANKING OPERATORS ***/
/***********************************/

// HERE HERE HERE -->  Add more addOperandToBooleanQuery(bq) methods to all items below

builder.max = {ctx, op, operands ->
  DisjunctionMaxQuery maxOp = new DisjunctionMaxQuery(0.0f);
  operands.each{ maxOp.add(toQuery(it)) };
  if(op.hasBoost()) maxOp.setBoost(op.boost);
  return maxOp;
};


builder.constant = {ctx, op, operands ->
  Query luceneOp = null;
  if(operands.size() > 1) {
    luceneOp = new BooleanQuery();
    operands.each{ luceneOp.add(toQuery(it), BooleanClause.Occur.SHOULD) };
  }
  else if(operands.size() == 1) {
    luceneOp = toQuery(operands.get(0));
  }
  else {
    throw new Exception("Illegal constant() operator with zero operands");
  }
  
  ConstantScoreQuery constantOp = new ConstantScoreQuery(luceneOp);
  if(op.hasBoost()) constantOp.setBoost(op.boost);
  return constantOp;
};


builder.boostPlus = {ctx, op, operands ->
  BooleanQuery bq = new BooleanQuery();
  boolean first = true;
  operands.each {
    bq.add(toQuery(it), first ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD);
    first = false;
  };
  if(op.hasBoost()) bq.setBoost(op.boost);
  return bq;
};


builder.boostMul = {QPLBuilder ctx, Operator op, List<Object> operands ->
  Query positiveQ = null;
  Query otherQ = null;

  for(int operandCount = 0 ; operandCount < operands.size() ; operandCount++) {
    Object operand = operands.get(operandCount);
    
    if(operandCount == 0) positiveQ = toQuery(operand);
    else if(operandCount == 1) {
      otherQ = toQuery(operand);
    }
    else if(operandCount == 2) {
      BooleanQuery tmpBq = new BooleanQuery();
      tmpBq.add(otherQ, BooleanClause.Occur.SHOULD);
      tmpBq.add(toQuery(operand), BooleanClause.Occur.SHOULD);
      otherQ = tmpBq;
    }
    else {
      otherQ.add(toQuery(operand), BooleanClause.Occur.SHOULD);
    }
  };
  
  if(otherQ == null)
    return positiveQ;
  else
    return new BoostingQuery(positiveQ, otherQ, op.boostOrDefault);
};



builder.orMin = {ctx, op, operands -> 
  BooleanQuery bq = new BooleanQuery();
  operands.each{ bq.add(toQuery(it), BooleanClause.Occur.SHOULD) };
  if(op.hasBoost()) bq.setBoost(op.boost);
  def minClauses = op.get("minClauses");
  bq.setMinimumNumberShouldMatch(minClauses.intValue());
  return bq;
};




/**********************/
/** HANDLING PHRASES **/
/**********************/

/***
 * In order to handle Lucene MultiPhrase, I wanted to allow for phrases with nested OR's, as well as phrases
 * with nested phrases, not just a list of embedded terms. Also, I didn't want the system to build a lot of
 * non-essential TermQuery objects (the children of the phrase), since MultiPhraseQuery just takes Term() objects
 * (and not TermQuery objects).
 * 
 * Therefore, we intercept the phrase at the parent level and accumulate all of the children as needed, in other
 * words, a top-down construction rather than a bottoms-up construction.
 */

def accumulateNestedOrTerms(QPLBuilder ctx, termsList, Operator orOp) {
  if(orOp.numOperands > 0) {
    orOp.operands.each { 
      if(it.type == TERM) {
        termsList.add(new Term(ctx.fieldOrDefault, it.term));
      }
      else if(it.type == OR) {
        accumulateNestedOrTerms(ctx, termsList, it);
      }
      else {
        throw new Exception("Lucene Builder:  Phrase expression contains nested or() which has something other than nested terms and other nested or()s. (" + it.type.name + ")");
      }
    };
  }
}

def processPhraseItem(QPLBuilder ctx, operandsList, item) {
  if(item.type == TERM) {
    if (item.field == null) 
      operandsList.add(new Term(ctx.fieldOrDefault, item.term));
    else
      operandsList.add(new Term(item.field, item.term));
  }
  else if(item.type == PHRASE) {
    if( item.numOperands > 0)
      item.operands.each { processPhraseItem(ctx, operandsList, it); };
  }
  else if (item.type == WILDCARD) {
    if (ctx.getEngineContext() == null) {
      operandsList.add(new Term(ctx.fieldOrDefault, item.term.replaceAll("\\*|\\?","")));
    } else {
      if (item.field == null) 
        wTerm = new Term(ctx.fieldOrDefault, item.term)
      else
        wTerm = new Term(item.field, item.term)

	  Fields fields = MultiFields.getFields((IndexReader)ctx.getEngineContext());
	  Terms terms = fields.terms(wTerm.field());
	  Automaton automaton = WildcardQuery.toAutomaton(wTerm);
	  CompiledAutomaton compiledAutomaton = new CompiledAutomaton(automaton);
	  TermsEnum termsEnum = compiledAutomaton.getTermsEnum(terms);
	  int counter = 0;
	  for(;;) {
		BytesRef bRef = termsEnum.next();
		if(bRef == null) { 
			break; 
		}
		terms.add(bRef.utf8ToString());
		if (counter++ >= 100) {
			println "Wildcard expansion exceeds limit, ignored" + term.text()
			break;
		}
	  }
  	  if (terms.size() > 0) {
  	    Term[] termsAry = terms.toArray(new Term[terms.size()]);
  	    operandsList.add(termsAry);
  	  }
    }
  }
  else if(item.type == OR) {
    def termsList = [];
    accumulateNestedOrTerms(ctx, termsList, item);
    
    if(termsList.size() > 0) {
      //* Create a terms array
      Term[] termsArray = new Term[termsList.size()];
      termsList.eachWithIndex { it, i -> termsArray[i] = it; }
      operandsList.add(termsArray);
    }
  }
  else {
    throw new Exception("Lucene Builder:  Phrase expression contains something other than TERM, PHRASE, or OR (" + it.type.name + ")");
  }
}

builder.'phrase@parent' = {ctx, op -> 
  if(op.numOperands > 0) {
	def operandsList = [];
    op.operands.each { processPhraseItem(ctx, operandsList, it) };
    
    def phraseOp;
    if( operandsList.any { it instanceof Term[] } )
      phraseOp = new MultiPhraseQuery();
    else
      phraseOp = new PhraseQuery();
    
    operandsList.each{ phraseOp.add(it); }
    
    if(op.hasBoost()) phraseOp.setBoost(op.boost);
    return phraseOp;
  }
};

/**********************/
/**    DATE RANGES   **/
/**********************/
builder.'range@parent' = {ctx, op ->
	def lowerLimit = op.getRangeFrom();
	def upperLimit = op.getRangeTo();
	def lowerLimitBytes =  null;
	def upperLimitBytes = null;
	def rangeOp = null;
	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
	if(lowerLimit instanceof Date || upperLimit instanceof Date){
		
		QParser qplParser = (QParser)ctx.getParser();
 		IndexSchema schema = (IndexSchema)ctx.getSchema();
  		SchemaField field = schema.getField(ctx.fieldOrDefault);  
  		TrieDateField tdf = (TrieDateField)field.getType();
  		Query query = tdf.getRangeQuery(qplParser, field,lowerLimit,upperLimit,true,true);
  		return query;
			
	 }
  else if(lowerLimit instanceof String || upperLimit instanceof String){
    if(lowerLimit != null){
       lowerLimitBytes = lowerLimit.getBytes();
    }
    if(upperLimit != null){
      upperLimitBytes = upperLimit.getBytes();
    }
    rangeOp = new TermRangeQuery(ctx.fieldOrDefault, new BytesRef(lowerLimitBytes), new BytesRef(upperLimitBytes), true, true);
   }
   else if(lowerLimit instanceof Float || upperLimit instanceof Float || lowerLimit instanceof Integer || upperLimit instanceof Integer){
    QParser qplParser = (QParser) ctx.getParser();
    IndexSchema schema = (IndexSchema) ctx.getSchema();
      SchemaField field = schema.getField(ctx.fieldOrDefault);
      
      part1 = lowerLimit ? lowerLimit.toString() : null;
      part2 = upperLimit ? upperLimit.toString() : null;
      
      rangeOp = field.getType().getRangeQuery(qplParser,field,part1,part2,true,true);
   }
 	 return rangeOp;
  };
