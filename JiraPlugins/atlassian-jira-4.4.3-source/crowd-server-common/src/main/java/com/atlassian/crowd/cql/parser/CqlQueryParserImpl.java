package com.atlassian.crowd.cql.parser;

import com.atlassian.crowd.cql.parser.antlr.CqlEval;
import com.atlassian.crowd.cql.parser.antlr.CqlLexer;
import com.atlassian.crowd.cql.parser.antlr.CqlParser;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.PropertyTypeService;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * An implementation of CqlQueryParser
 *
 * @since 2.2
 */
public class CqlQueryParserImpl implements CqlQueryParser
{
    public SearchRestriction parseQuery(final String restriction, final PropertyTypeService propertyTypeService)
    {
        try
        {
            return createCqlParser(restriction, propertyTypeService).getRestriction();
        }
        catch (RecognitionException e)
        {
            throw new IllegalArgumentException("Unknown query", e);
        }
    }

    private CqlEval createCqlParser(final String clauseString, final PropertyTypeService propertyTypeService) throws RecognitionException
    {
        final CqlLexer lexer = new CqlLexer(new ANTLRStringStream(clauseString));
        CqlParser parser = new CqlParser(new CommonTokenStream(lexer));
        CqlParser.restriction_return r = parser.restriction();

        // WALK RESULTING TREE
        CommonTree t = (CommonTree)r.getTree();// get tree from parser
        // Create a tree node stream from resulting tree
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        CqlEval cqlEval = new CqlEval(nodes);
        cqlEval.setPropertyTypeService(propertyTypeService);
        return cqlEval;
    }
}
