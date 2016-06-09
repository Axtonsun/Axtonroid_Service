// MyAIDLService.aidl
package com.example.axtonsun.axtonroid_service;

// Declare any non-default types here with import statements

interface MyAIDLService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     int plus(int a, int b);
     String toUpperCase(String str);
}
