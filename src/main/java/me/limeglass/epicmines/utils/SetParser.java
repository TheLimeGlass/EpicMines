package me.limeglass.epicmines.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Material;

public class SetParser {

	private final String input;

	public SetParser(String... arguments) {
		input = String.join(" ", arguments);
	}

	public Map<String, ParseResult> parse() {
		String[] elements = input.split(Pattern.quote(","));
		Map<String, ParseResult> results = new HashMap<>();
		for (String element : elements) {
			if (element == null)
				continue;
			element = element.trim();
			if (element.equals(""))
				continue;
			if (!element.contains(":")) {
				results.put(element, new ParseResult(Type.SYNTAX_ERROR));
				continue;
			}
			String[] parse = element.split(Pattern.quote(":"));
			String prep = parse[0].toUpperCase(Locale.US);
			prep = prep.replaceAll(" ", "_");
			Material material = Material.getMaterial(prep);
			if (material == null) {
				results.put(element, new ParseResult(Type.MATERIAL).setInput(parse[0]));
				continue;
			}
			if (!material.isBlock()) {
				results.put(element, new ParseResult(Type.BLOCK).setMaterial(material).setInput(parse[0]));
				continue;
			}
			double chance;
			try {
				chance = Double.parseDouble(parse[1].replaceAll(Pattern.quote("%"), ""));
			} catch (NumberFormatException e) {
				results.put(element, new ParseResult(Type.NUMBER).setInput(parse[1]));
				continue;
			}
			if (chance < 0 || chance > 100) {
				results.put(element, new ParseResult(Type.NUMBER).setInput(parse[1]));
				continue;
			}
			ParseResult success = new ParseResult(Type.SUCCESS);
			success.setMaterial(material);
			success.setChance(chance);
			results.put(element, success);
		}
		return results;
	}

	public enum Type {
		SYNTAX_ERROR,
		MATERIAL,
		SUCCESS,
		NUMBER,
		BLOCK;
	}

	public class ParseResult {

		private final Type type;
		private Material material;
		private double chance;
		private String input;

		public ParseResult(Type type) {
			this.type = type;
		}

		public ParseResult setMaterial(Material material) {
			this.material = material;
			return this;
		}

		public Material getMaterial() {
			return material;
		}

		public ParseResult setInput(String input) {
			this.input = input;
			return this;
		}

		public String getInput() {
			return input;
		}

		public ParseResult setChance(double chance) {
			this.chance = chance;
			return this;
		}

		public double getChance() {
			return chance;
		}

		public Type getType() {
			return type;
		}

	}

}
