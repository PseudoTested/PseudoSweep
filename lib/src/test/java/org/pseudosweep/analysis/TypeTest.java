package org.pseudosweep.analysis;

import org.junit.jupiter.api.Test;
import org.pseudosweep.analysis.sdl.TypeSDL;
import org.pseudosweep.analysis.xmt.TypeXMT;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TypeTest {

    @Test
    void testTypeXMT() {
        Type typeXMT = TypeXMT.METHOD;
        assertInstanceOf(Type.class, typeXMT);
        assertInstanceOf(TypeXMT.class, typeXMT);
    }

    @Test
    void testTypeSDL() {
        Type typeSDL = TypeSDL.EXPRESSION;
        assertInstanceOf(Type.class, typeSDL);
        assertInstanceOf(TypeSDL.class, typeSDL);
    }

}