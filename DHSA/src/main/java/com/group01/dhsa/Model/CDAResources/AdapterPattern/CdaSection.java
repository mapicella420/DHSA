package com.group01.dhsa.Model.CDAResources.AdapterPattern;

public interface CdaSection<T, U> {
    T toCdaObject(U fhirObject);
}
