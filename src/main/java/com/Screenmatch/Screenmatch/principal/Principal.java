package com.Screenmatch.Screenmatch.principal;

import com.Screenmatch.Screenmatch.model.DadosEpisodio;
import com.Screenmatch.Screenmatch.model.DadosSerie;
import com.Screenmatch.Screenmatch.model.DadosTemporada;
import com.Screenmatch.Screenmatch.model.Episodio;
import com.Screenmatch.Screenmatch.service.ConsumoAPI;
import com.Screenmatch.Screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=fe69c902";
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu() {
        Scanner entrada = new Scanner(System.in);

        System.out.println("Digite o nome da série: ");
        var nomeSerie = entrada.nextLine();
        var json = consumo.obterDados(ENDERECO+nomeSerie.replace(" ", "+")+API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();
		for (int i = 1; i<=dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO+nomeSerie.replace(" ", "+")+"&season="+ i +API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        for (int i = 0; i< dados.totalTemporadas();i++){
            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for(int j = 0; j<episodiosTemporada.size();j++){
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }

        temporadas.forEach(t-> t.episodios().forEach(e-> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());
//        System.out.println("\nTop 10 Episódios");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .limit(10)
//                .map(e-> e.titulo().toUpperCase())
//                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d-> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());
        episodios.forEach(System.out::println);

        System.out.println("Digite o nome do episodio ");
        var trechoDoTitulo = entrada.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoDoTitulo.toUpperCase()))
                .findFirst();
        if(episodioBuscado.isPresent()){
            System.out.println("Episodio encontrado na temporada " + episodioBuscado.get().getTemporada());
        }else{
            System.out.println("Episodio não encontrado");
        }
//
//        System.out.println("Mostrar a partir de que ano?");
//        var ano = entrada.nextInt();
//        entrada.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream().filter(e->e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e-> System.out.println(
//                        "Temporada " + e.getTemporada()+
//                                " Episodio " + e.getTitulo() +
//                                " Data de Lancamento " + e.getDataLancamento().format(formatador)
//                ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e->e.getAvaliacao()>0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e->e.getAvaliacao()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + est.getAverage()
                + "\nMelhor episodio:  "+ est.getMax()
                + "\nPior Episodio: "+ est.getMin()
                + "\nQuantidade de avaliados: " + est.getCount());
    }
}
