/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {Chart, ChartJsRenderer} from '../../src/index';

describe('ChartJsRendererSpec', () => {

  describe('_adjustGridMaxMin', () => {
    let renderer = new ChartJsRenderer({}),
      chartArea = {
        top: 0,
        bottom: 300,
        left: 0,
        right: 750
      },
      defaultConfig = {
        data: {
          datasets: [{
            data: [11, 13, 17, 31, 37, 41, 43],
            label: 'Dataset 1'
          }]
        },
        options: {
          adjustGridMaxMin: true
        }
      },
      defaultScalesConfig = $.extend(true, {}, defaultConfig, {
        options: {
          scales: {
            xAxes: [{}],
            yAxes: [{}]
          }
        }
      }),
      defaultScaleConfig = $.extend(true, {}, defaultConfig, {
        options: {
          scale: {}
        }
      });

    it('bar chart, min/max is set on y axis', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {type: Chart.Type.BAR});

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.xAxes[0]).toEqual({});
      expect(config.options.scales.yAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 8,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 45,
          suggestedMin: 10
        }
      });
    });

    it('horizontal bar chart, min/max is set on x axis', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {type: Chart.Type.BAR_HORIZONTAL});

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.xAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 5,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 45,
          suggestedMin: 10
        }
      });
      expect(config.options.scales.yAxes[0]).toEqual({});
    });

    it('polar area chart, min/max is set on scale', () => {
      let config = $.extend(true, {}, defaultScaleConfig, {type: Chart.Type.POLAR_AREA});

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scale).toEqual({
        ticks: {
          maxTicksLimit: 4,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 45,
          suggestedMin: 10
        }
      });
    });

    it('bubble chart, min/max is set on x and y axis, axis without offset take max(r) into account', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {
        type: Chart.Type.BUBBLE,
        options: {
          scales: {
            xAxes: [
              {
                offset: true
              }
            ]
          }
        }
      });

      config.data.datasets[0].data = [
        {x: 11, y: 37, r: 47},
        {x: 13, y: 17, r: 29},
        {x: 17, y: 41, r: 19},
        {x: 31, y: 11, r: 53},
        {x: 37, y: 31, r: 13},
        {x: 41, y: 43, r: 11},
        {x: 43, y: 13, r: 37}
      ];

      renderer._adjustGridMaxMin(config, chartArea);

      let height = Math.abs(chartArea.top - chartArea.bottom),
        padding = 53, // max(r)
        maxY = 43,
        minY = 11,
        yValuePerPixel = (maxY - minY) / (height - 2 * padding),
        yPaddingValue = yValuePerPixel * padding;

      expect(config.options.scales.xAxes[0]).toEqual({
        offset: true,
        ticks: {
          maxTicksLimit: 5,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 46,
          suggestedMin: 11
        }
      });
      expect(config.options.scales.yAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 8,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 43 + yPaddingValue,
          suggestedMin: 11 - yPaddingValue
        }
      });
    });

    it('bubble chart, min/max is set on x and y axis, axis without offset take max(r) into account, axis with labelMap calculate exact min/max', () => {

      let labelMap = {
          11: 'Label 11',
          13: 'Label 13',
          17: 'Label 17',
          31: 'Label 31',
          37: 'Label 37',
          41: 'Label 41',
          43: 'Label 43'
        },
        config = $.extend(true, {}, defaultScalesConfig, {
          type: Chart.Type.BUBBLE,
          options: {
            scales: {
              xLabelMap: labelMap,
              yLabelMap: labelMap,
              xAxes: [
                {
                  offset: true
                }
              ]
            }
          }
        });

      config.data.datasets[0].data = [
        {x: 11, y: 37, r: 47},
        {x: 13, y: 17, r: 29},
        {x: 17, y: 41, r: 19},
        {x: 31, y: 11, r: 53},
        {x: 37, y: 31, r: 13},
        {x: 41, y: 43, r: 11},
        {x: 43, y: 13, r: 37}
      ];

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.xAxes[0]).toEqual({
        offset: true,
        ticks: {
          maxTicksLimit: 5,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 43,
          suggestedMin: 11
        }
      });
      expect(config.options.scales.yAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 8,
          stepSize: 1, // default value, not part of this test
          suggestedMax: 52,
          suggestedMin: 2
        }
      });
    });
  });

  describe('_adjustBubbleSizes', () => {
    let renderer = new ChartJsRenderer({}),
      chartArea = {
        top: 0,
        bottom: 300,
        left: 0,
        right: 750
      },
      defaultConfig = {
        type: Chart.Type.BUBBLE,
        data: {
          datasets: [{
            data: [
              {z: 100},
              {z: 81},
              {z: 49},
              {z: 25},
              {z: 16}
            ],
            label: 'Dataset 1'
          }]
        },
        bubble: {}
      };

    it('neither sizeOfLargestBubble nor minBubbleSize is set', () => {
      let config = $.extend(true, {}, defaultConfig);

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 10, z: 100},
        {r: 9, z: 81},
        {r: 7, z: 49},
        {r: 5, z: 25},
        {r: 4, z: 16}
      ]);
    });

    it('only sizeOfLargestBubble is set', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.bubble.sizeOfLargestBubble = 20;

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 20, z: 100},
        {r: 18, z: 81},
        {r: 14, z: 49},
        {r: 10, z: 25},
        {r: 8, z: 16}
      ]);
    });

    it('only sizeOfLargestBubble is set, maxR equals 0', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.bubble.sizeOfLargestBubble = 20;

      config.data.datasets[0].data = [
        {z: 0},
        {z: 0},
        {z: 0}
      ];

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 20, z: 0},
        {r: 20, z: 0},
        {r: 20, z: 0}
      ]);
    });

    it('only minBubbleSize is set', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.bubble.minBubbleSize = 5;

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 12.5, z: 100},
        {r: 11.25, z: 81},
        {r: 8.75, z: 49},
        {r: 6.25, z: 25},
        {r: 5, z: 16}
      ]);
    });

    it('only minBubbleSize is set, minR equals 0', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.bubble.minBubbleSize = 5;

      config.data.datasets[0].data.push({z: 0});

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 15, z: 100},
        {r: 14, z: 81},
        {r: 12, z: 49},
        {r: 10, z: 25},
        {r: 9, z: 16},
        {r: 5, z: 0}
      ]);
    });

    it('sizeOfLargestBubble and minBubbleSize are set, scaling is sufficient', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.bubble.sizeOfLargestBubble = 20;
      config.bubble.minBubbleSize = 5;

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 20, z: 100},
        {r: 18, z: 81},
        {r: 14, z: 49},
        {r: 10, z: 25},
        {r: 8, z: 16}
      ]);
    });

    it('sizeOfLargestBubble and minBubbleSize are set, scaling is not sufficient', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.bubble.sizeOfLargestBubble = 20;
      config.bubble.minBubbleSize = 5;

      config.data.datasets[0].data.push({z: 1});

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {r: 20, z: 100},
        {r: 9 * (5 / 3) + (10 / 3), z: 81},
        {r: 15, z: 49},
        {r: 5 * (5 / 3) + (10 / 3), z: 25},
        {r: 10, z: 16},
        {r: 5, z: 1}
      ]);
    });
  });
});
