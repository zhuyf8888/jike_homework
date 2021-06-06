package zhuyf.spring.springbean01.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zhuyf.spring.springbean01.bean.Person;

/**
 * 通过注解创建对象
 */
@Component
public class PersonAnnotation {

    @Autowired
    private Person person;

    public void test() {
    }
}
