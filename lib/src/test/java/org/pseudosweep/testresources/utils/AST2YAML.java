package org.pseudosweep.testresources.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.YamlPrinter;

public class AST2YAML {

    public static void printYAMLOfCompilationUnit(String code) {
        CompilationUnit node = StaticJavaParser.parse(code);
        YamlPrinter printer = new YamlPrinter(true);
        System.out.println(printer.output(node));
    }

    public static void printYAMLOfStatement(String code) {
        Statement node = StaticJavaParser.parseStatement(code);
        YamlPrinter printer = new YamlPrinter(true);
        System.out.println(printer.output(node));
    }

    public static void printYAMLOfExpression(String code) {
        Expression node = StaticJavaParser.parseExpression(code);
        YamlPrinter printer = new YamlPrinter(true);
        System.out.println(printer.output(node));
    }
}
