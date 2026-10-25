package com.netby.evaluacion.resource;

import com.netby.evaluacion.resource.ApiDePrueba.CuentaCreada;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.netby.evaluacion.resource.ApiDePrueba.crearCuentaActiva;
import static com.netby.evaluacion.resource.ApiDePrueba.saldoDe;
import static com.netby.evaluacion.resource.ApiDePrueba.transferir;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TransferenciaConcurrenciaTest {

    private static final int HILOS = 10;

    private static List<Integer> ejecutarEnParalelo(List<Callable<Integer>> tareas) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tareas.size());
        try {
            CountDownLatch largada = new CountDownLatch(1);
            List<Future<Integer>> futuros = new ArrayList<>();
            for (Callable<Integer> tarea : tareas) {
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return tarea.call();
                }));
            }
            largada.countDown();

            List<Integer> codigos = new ArrayList<>();
            for (Future<Integer> futuro : futuros) {
                codigos.add(futuro.get(60, TimeUnit.SECONDS));
            }
            return codigos;
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void transferenciasSimultaneasNoDebenDejarSaldoNegativoNiPerderActualizaciones() throws Exception {
        CuentaCreada origen = crearCuentaActiva("1000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");

        List<Callable<Integer>> tareas = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            tareas.add(() -> transferir(origen, destino, "200.00").statusCode());
        }

        List<Integer> codigos = ejecutarEnParalelo(tareas);

        // El saldo solo alcanza para 5 transferencias
        assertThat(codigos).filteredOn(c -> c == 201).hasSize(5);
        assertThat(codigos).filteredOn(c -> c == 422).hasSize(5);
        assertThat(saldoDe(origen)).isEqualByComparingTo("0.00");
        assertThat(saldoDe(destino)).isEqualByComparingTo("1000.00");
    }

    @Test
    void transferenciasCruzadasSimultaneasNoDebenProvocarDeadlocks() throws Exception {
        CuentaCreada cuentaA = crearCuentaActiva("1000.00");
        CuentaCreada cuentaB = crearCuentaActiva("1000.00");

        List<Callable<Integer>> tareas = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            tareas.add(() -> transferir(cuentaA, cuentaB, "10.00").statusCode());
            tareas.add(() -> transferir(cuentaB, cuentaA, "10.00").statusCode());
        }

        List<Integer> codigos = ejecutarEnParalelo(tareas);

        assertThat(codigos).containsOnly(201);
        assertThat(saldoDe(cuentaA).add(saldoDe(cuentaB))).isEqualByComparingTo("2000.00");
        assertThat(saldoDe(cuentaA)).isEqualByComparingTo("1000.00");
    }
}
