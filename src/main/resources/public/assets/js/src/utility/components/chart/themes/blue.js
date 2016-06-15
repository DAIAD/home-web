var theme = {
  color : [
      '#1790cf', '#1bb2d8', '#99d2dd', '#88b0bb', '#1c7099', '#038cc4', '#75abd0', '#afd6dd'
  ],
  title : {
    textStyle : {
      fontWeight : 'normal',
      color : '#1790cf'
    }
  },
  dataRange : {
    color : [
        '#1178ad', '#72bbd0'
    ]
  },
  toolbox : {
    color : [
        '#1790cf', '#1790cf', '#1790cf', '#1790cf'
    ]
  },
  tooltip : {
    backgroundColor : 'rgba(0,0,0,0.5)',
    axisPointer : {
      type : 'line',
      lineStyle : {
        color : '#1790cf',
        type : 'dashed'
      },
      crossStyle : {
        color : '#1790cf'
      },
      shadowStyle : {
        color : 'rgba(200,200,200,0.3)'
      }
    }
  },
  dataZoom : {
    dataBackgroundColor : '#eee',
    fillerColor : 'rgba(144,197,237,0.2)',
    handleColor : '#1790cf'
  },
  grid : {
    borderWidth : 0
  },
  categoryAxis : {
    /*
    axisLine : {
      lineStyle : {
        color : '#1790cf'
      }
    },
    */
    splitLine : {
      lineStyle : {
        color : [
          '#eee'
        ]
      }
    }
  },
  valueAxis : {
    /*
    axisLine : {
      lineStyle : {
        color : '#1790cf'
      }
    },
    */
    splitArea : {
      show : true,
      areaStyle : {
        color : [
            'rgba(250,250,250,0.1)', 'rgba(200,200,200,0.1)'
        ]
      }
    },
    splitLine : {
      lineStyle : {
        color : [
          '#eee'
        ]
      }
    }
  },
  timeline : {
    lineStyle : {
      color : '#1790cf'
    },
    controlStyle : {
      normal : {
        color : '#1790cf'
      },
      emphasis : {
        color : '#1790cf'
      }
    }
  },
  k : {
    itemStyle : {
      normal : {
        color : '#1bb2d8',
        color0 : '#99d2dd',
        lineStyle : {
          width : 1,
          color : '#1c7099',
          color0 : '#88b0bb'
        }
      }
    }
  },
  map : {
    itemStyle : {
      normal : {
        areaStyle : {
          color : '#ddd'
        },
        label : {
          textStyle : {
            color : '#c12e34'
          }
        }
      },
      emphasis : {
        areaStyle : {
          color : '#99d2dd'
        },
        label : {
          textStyle : {
            color : '#c12e34'
          }
        }
      }
    }
  },
  force : {
    itemStyle : {
      normal : {
        linkStyle : {
          color : '#1790cf'
        }
      }
    }
  },
  chord : {
    padding : 4,
    itemStyle : {
      normal : {
        borderWidth : 1,
        borderColor : 'rgba(128, 128, 128, 0.5)',
        chordStyle : {
          lineStyle : {
            color : 'rgba(128, 128, 128, 0.5)'
          }
        }
      },
      emphasis : {
        borderWidth : 1,
        borderColor : 'rgba(128, 128, 128, 0.5)',
        chordStyle : {
          lineStyle : {
            color : 'rgba(128, 128, 128, 0.5)'
          }
        }
      }
    }
  },
  gauge : {
    axisLine : {
      show : true,
      lineStyle : {
        color : [
            [
                0.2, '#1bb2d8'
            ], [
                0.8, '#1790cf'
            ], [
                1, '#1c7099'
            ]
        ],
        width : 8
      }
    },
    axisTick : {
      splitNumber : 10,
      length : 12,
      lineStyle : {
        color : 'auto'
      }
    },
    axisLabel : {
      textStyle : {
        color : 'auto'
      }
    },
    splitLine : {
      length : 18,
      lineStyle : {
        color : 'auto'
      }
    },
    pointer : {
      length : '90%',
      color : 'auto'
    },
    title : {
      textStyle : {
        color : '#333'
      }
    },
    detail : {
      textStyle : {
        color : 'auto'
      }
    }
  },
  textStyle : {
    fontFamily : 'Arial, Verdana, sans-serif'
  }
};

module.exports = theme;
