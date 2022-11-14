package com.nowcoder.community.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
//
//@Component
//@Aspect
public class AlphaAspect {
    //service下的所有包的所有类（第一个.*），所有方法（第二个.*），所有参数（(..)）
    //com 前的*表示所有返回值
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public  void pointcut(){

    }
}
