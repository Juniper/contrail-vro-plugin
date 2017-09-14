/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;




public class GreetingService  {

   public GreetingService() {
       
   }
   
   public String greet(String name) {
       return "Hello, " + name +"! Welcome to 'Contrail' plug-in";
   }
}

