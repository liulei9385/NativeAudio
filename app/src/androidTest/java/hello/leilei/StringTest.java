package hello.leilei;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import hello.leilei.utils.FileUtils;

/**
 * Created by liulei
 * DATE: 2016/11/25
 * TIME: 9:54
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StringTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setup() {
    }

    @Test
    public void testHello() {
        String a = "leilei/leilei/a.txt";
        File sdCacheFile = FileUtils.createSdCacheFile(mActivityRule.getActivity(), a);
        System.out.println("StringTest.testStringSub" + sdCacheFile.getPath());
    }


}