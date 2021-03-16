package io.freethejazz.paintmix

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaintmixApplication

fun main(args: Array<String>) {
	runApplication<PaintmixApplication>(*args)
}
