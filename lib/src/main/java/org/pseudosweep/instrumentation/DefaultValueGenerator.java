package org.pseudosweep.instrumentation;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import org.pseudosweep.PseudoSweepException;

import static org.pseudosweep.util.StringUtils.encloseQuotes;

public class DefaultValueGenerator {

    public static Expression defaultValueExpr(Type type, boolean defaultSet) {
        if (defaultSet) {
            if (type.isPrimitiveType()) {
                PrimitiveType.Primitive primitiveType = type.asPrimitiveType().getType();
                return switch (primitiveType) {
                    case BOOLEAN -> new BooleanLiteralExpr(false);
                    case BYTE -> new CastExpr(type, new DoubleLiteralExpr("0"));
                    case DOUBLE, FLOAT -> new DoubleLiteralExpr("0");
                    case CHAR -> new CharLiteralExpr('\40');
                    case SHORT -> new CastExpr(type, new IntegerLiteralExpr("0"));
                    case INT -> new IntegerLiteralExpr("0");
                    case LONG -> new LongLiteralExpr("0");
                };
            }

            // String type
            if (type.toString().equals("String")) {
                return new StringLiteralExpr("");
            }


            // Empty arrays
            if (type.isArrayType() && !type.asArrayType().getElementType().isClassOrInterfaceType()) {

                ArrayCreationExpr arrayCreationExpr = new ArrayCreationExpr();
                arrayCreationExpr.setElementType(type.asArrayType().getElementType());

                NodeList<ArrayCreationLevel> levels = new NodeList<>();
                for (int i = 0; i < type.asArrayType().getArrayLevel(); i++) levels.add(new ArrayCreationLevel());
                arrayCreationExpr.setLevels(levels);

                return arrayCreationExpr;
            }


        } else {
            if (type.isPrimitiveType()) {
                PrimitiveType.Primitive primitiveType = type.asPrimitiveType().getType();
                return switch (primitiveType) {
                    case BOOLEAN -> new BooleanLiteralExpr(true);
                    case BYTE -> new CastExpr(type, new DoubleLiteralExpr("1"));
                    case DOUBLE, FLOAT -> new DoubleLiteralExpr("1");
                    case CHAR -> new CharLiteralExpr('A');
                    case SHORT -> new CastExpr(type, new IntegerLiteralExpr("1"));
                    case INT -> new IntegerLiteralExpr("1");
                    case LONG -> new LongLiteralExpr("1");
                };
            }

            // String type
            if (type.toString().equals("String")) {
                return new StringLiteralExpr("A");
            }

            // Collections
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("Collection")) {
                ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
                objectCreationExpr.setType("java.util.ArrayList");
                return objectCreationExpr;
            }

            // Iterables
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("Iterable")) {
                ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
                objectCreationExpr.setType("java.util.ArrayList");
                return objectCreationExpr;
            }

            // Lists
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("List")) {
                ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
                objectCreationExpr.setType("java.util.ArrayList");
                return objectCreationExpr;
            }

            // Queues -> look at common types for queues
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("Queue")) {
                ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
                objectCreationExpr.setType("java.util.LinkedList");
                return objectCreationExpr;
            }

            // Sets
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("Set")) {
                ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
                objectCreationExpr.setType("java.util.HashSet");
                return objectCreationExpr;
            }

            // Maps
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("Map")) {
                ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
                objectCreationExpr.setType("java.util.HashMap");
                return objectCreationExpr;
            }

        }
        if (type.isReferenceType()) {
            return new NullLiteralExpr();
        }
        throw new PseudoSweepException("Cannot create default value expression for type: " + encloseQuotes(type));

    }






}
