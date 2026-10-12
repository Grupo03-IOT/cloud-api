package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.util.List;

/**
 * @param total cuántos episodios hubo, aunque {@code worst} venga recortada
 * @param worst los de mayor pico, del más fuerte al más débil
 */
public record NoiseEpisodes(int total, List<NoiseEpisode> worst) {}
