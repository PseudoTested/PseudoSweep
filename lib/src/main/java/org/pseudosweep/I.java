package org.pseudosweep;

import org.pseudosweep.program.Block;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;

public class I {

    public static boolean exec(String typeStr, int id, String containingClass, String operatorSet) {
        if (operatorSet.equals("sdl")) {
            return execSDL(typeStr, id, containingClass);
        } else if (operatorSet.equals("xmt")) {
            return execXMT(typeStr, id, containingClass);
        }
        throw new PseudoSweepException("Unrecognised operator set");
    }

    public static void log(String typeStr, int id, String containingClass, String operatorSet) {
        if (operatorSet.equals("sdl")) {
            logSDL(typeStr, id, containingClass);
        } else if (operatorSet.equals("xmt")) {
            logXMT(typeStr, id, containingClass);
        }
    }

    public static boolean defaultValue(String typeStr, int id, String containingClass, String operatorSet) {
        if (operatorSet.equals("sdl")) {
            return defaultValueSDL(typeStr, id, containingClass);
        } else if (operatorSet.equals("xmt")) {
            return defaultValueXMT(typeStr, id, containingClass);
        }
        throw new PseudoSweepException("Unrecognised operator set");
    }

    public static boolean fix(boolean truthValue, String typeStr, int id, String containingClass) {
        Decision decision = new Decision(truthValue, Decision.Type.valueOf(typeStr), id, containingClass);
        return TestExecutionManager.instance().getAuditor().fix(decision);
    }

    public static boolean eval() {
        if (TestExecutionManager.instance().getAuditor() != null) {
            return TestExecutionManager.instance().getAuditor().eval();
        }
        throw new PseudoSweepException("Auditor is null");

    }

    private static boolean execSDL(String typeStr, int id, String containingClass) {
        Stmt stmt = new Stmt(Stmt.Type.valueOf(typeStr), id, containingClass, true);
        if (TestExecutionManager.instance().getAuditor() != null) {
            return TestExecutionManager.instance().getAuditor().execSDL(stmt);
        }
        throw new PseudoSweepException("Auditor for " + stmt + " is null");
    }

    private static void logSDL(String typeStr, int id, String containingClass) {
        System.out.println("LOG");
        Stmt stmt = new Stmt(Stmt.Type.valueOf(typeStr), id, containingClass, true);
        TestExecutionManager.instance().getAuditor().log(stmt);
    }

    private static boolean defaultValueSDL(String typeStr, int id, String containingClass) {
        Stmt stmt = new Stmt(Stmt.Type.valueOf(typeStr), id, containingClass, true);
        return TestExecutionManager.instance().getAuditor().defaultValueSDL(stmt);
    }

    private static boolean execXMT(String typeStr, int id, String containingClass) {
        Block block = new Block(Block.Type.valueOf(typeStr), id, containingClass, true);
        return TestExecutionManager.instance().getAuditor().execXMT(block);
    }

    private static void logXMT(String typeStr, int id, String containingClass) {
        Block block = new Block(Block.Type.valueOf(typeStr), id, containingClass, true);
        block.setEmpty(true);
        TestExecutionManager.instance().getAuditor().log(block);
    }

    private static boolean defaultValueXMT(String typeStr, int id, String containingClass) {
        Block block = new Block(Block.Type.valueOf(typeStr), id, containingClass, true);
        return TestExecutionManager.instance().getAuditor().defaultValueXMT(block);
    }

}
