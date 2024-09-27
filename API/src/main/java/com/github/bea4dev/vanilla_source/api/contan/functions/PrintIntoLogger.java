package com.github.bea4dev.vanilla_source.api.contan.functions;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import org.contan_lang.ContanEngine;
import org.contan_lang.environment.Environment;
import org.contan_lang.evaluators.Evaluator;
import org.contan_lang.standard.functions.Print;
import org.contan_lang.syntax.tokens.Token;
import org.contan_lang.thread.ContanThread;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanVoidObject;

import java.util.logging.Logger;

public class PrintIntoLogger extends Print {
    private static final Logger logger = Logger.getLogger("Script");

    public PrintIntoLogger(ContanEngine contanEngine, Token functionName, Evaluator evaluator, Token... args) {
        super(contanEngine, functionName, evaluator, args);
    }

    @Override
    public ContanObject<?> eval(Environment environment, Token token, ContanThread contanThread, ContanObject<?>... contanObjects) {
        for (ContanObject<?> variable : contanObjects) {
            logger.info(variable.toString());
        }
        return ContanVoidObject.INSTANCE;
    }
}
