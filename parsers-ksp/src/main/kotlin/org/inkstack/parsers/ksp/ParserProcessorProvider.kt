package org.inkstack.parsers.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ParserProcessorProvider : SymbolProcessorProvider {

	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		val processor = ParserProcessor(
			codeGenerator = environment.codeGenerator,
			logger = environment.logger,
			options = environment.options,
		)
		// Opt into KSP 2.3+ backing-field / new language-feature modelling.
		environment.registerProcessorForNewFeatures(processor)
		return processor
	}
}
