package org.languagetool.rules.uk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
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
import org.json.*;

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
			if (spellerUkr.isMisspelled(word)) {
				String rusLayoutWord = word.replace('і', 'ы').replace('ї', 'ъ')
						.replace('є', 'э').replace('’', 'ё');
				
				if (!spellerRus.isMisspelled(rusLayoutWord)) {
					List<String> translations = dictionary.translate(rusLayoutWord);
					if (!translations.isEmpty()) {
						final RuleMatch ruleMatch = new RuleMatch(this,
								token.getStartPos(), token.getStartPos()
										+ word.length(),
								messages.getString("spelling"),
								messages.getString("desc_spelling_short"));
						ruleMatch.setSuggestedReplacements(translations);
						ruleMatches.add(ruleMatch);
					}
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

	private static final String API_KEY = "trnsl.1.1.20140329T130001Z.f13b8d6275523b4d.a3f09eeb63822950bfe29582053354925f1176";

	@Override
	public List<String> translate(String text) throws IOException {
		String addr = "https://translate.yandex.net/api/v1.5/tr.json/translate"
				+ "?key=trnsl.1.1.20140329T130001Z.f13b8d6275523b4d.a3f09eeb63822950bfe2958205335492835f1176&lang=ru-uk&format=plain&text="
				+ URLEncoder.encode(text, "UTF-8");
		URL url = new URL(addr);
		InputStream stream = url.openStream();
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(
				stream, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);
		JSONObject json = new JSONObject(responseStrBuilder.toString());
		String translation = json.getJSONArray("text").getString(0);
		stream.close();

		List<String> translations = new ArrayList<String>();
		translations.add(translation);
		return translations;
	}
}