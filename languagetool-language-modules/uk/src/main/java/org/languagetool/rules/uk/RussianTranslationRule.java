package org.languagetool.rules.uk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;

public class RussianTranslationRule extends Rule {

	private Dictionary dictionary = new YandexTranslate();
	protected MorfologikSpeller spellerUkr;
	protected MorfologikSpeller spellerRus;
	
	public RussianTranslationRule(final ResourceBundle messages) {
		super(messages);
	}

	@Override
	public String getId() {
		return "UK_RUSSIAN_TRANSLATION";
	}

	@Override
	public String getDescription() {
		return "Переклад россійських слів";
	}

	@Override
	public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
		List<RuleMatch> ruleMatches = new ArrayList<>();
		AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
		lazyInit();
		// toRuleMatchArray(ruleMatches);

		for (AnalyzedTokenReadings token : tokens) {
			String word = token.getToken();
			if (spellerUkr.isMisspelled(word) && !spellerRus.isMisspelled(word)) {
				List<String> translations = dictionary.translate(word);
				if (!translations.isEmpty()) {
					final RuleMatch ruleMatch = new RuleMatch(this,
							token.getStartPos(),
							token.getStartPos() + word.length(),
							messages.getString("spelling"),
							messages.getString("desc_spelling_short"));
					ruleMatch.setSuggestedReplacements(translations);
					ruleMatches.add(ruleMatch);
				}
			}
		}
		return toRuleMatchArray(ruleMatches);
	}

	@Override
	public void reset() {
	}

	private void lazyInit() throws IOException {
		if (spellerUkr == null) {
			spellerUkr = new MorfologikSpeller("/uk/hunspell/uk_UA.dict",
					new Locale("uk"), 1);
		}
		if (spellerRus == null) {
			spellerRus = new MorfologikSpeller("/ru/hunspell/ru_RU.dict",
					new Locale("ru"), 1);
		}
	}

}

interface Dictionary {
	public List<String> translate(String text) throws IOException;
}

class YandexTranslate implements Dictionary {

	@Override
	public List<String> translate(String text) throws IOException {
		String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
		List<String> translations = new ArrayList<String>();
		translations.add("hello!");
		return translations;
	}

}