package com.apicarv.testCarver.utils;

import org.junit.Assert;
import org.junit.Test;

public class UtilsOASGenTests {
    @Test
    public void removeBaseTest(){
        String base = UtilsOASGen.getPathFromURL("http://localhost:8080/api");
        String withoutBase = UtilsOASGen.removeBaseFromPath("/api/cart", base);
        Assert.assertTrue(withoutBase.equals("/cart"));
    }
}
