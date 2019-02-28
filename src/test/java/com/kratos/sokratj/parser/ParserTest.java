package com.kratos.sokratj.parser;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void testParse() throws IOException {

        new PhotoParser().parseData("data/a_example.txt");
    }
}