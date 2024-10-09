package com.Screenmatch.Screenmatch.service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
