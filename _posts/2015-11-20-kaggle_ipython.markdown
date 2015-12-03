---
layout: post
comments: true
title: Kaggle Titanic using python
date: 2015-11-20
PAGE_IDENTIFIER: kaggle_ipython
permalink: /kaggle-titanic.html
description: Kaggle Titanic challenge solution using python and graphlab create
---
###Load graphlab
<br/>
{% highlight python %}
import graphlab
{% endhighlight %}
<br/>

###Load the data
<br/>

{% highlight python %}
passengers = graphlab.SFrame('train.csv')

PROGRESS: Finished parsing file /Users/vishnu/git/hadoop/ipython/train.csv
PROGRESS: Parsing completed. Parsed 100 lines in 0.020899 secs.
------------------------------------------------------
Inferred types from first line of file as 
column_type_hints=[int,int,int,str,str,float,int,int,str,float,str,str]
If parsing fails due to incorrect types, you can correct
the inferred type list above and pass it to read_csv in
the column_type_hints argument
------------------------------------------------------
PROGRESS: Finished parsing file /Users/vishnu/git/hadoop/ipython/train.csv
PROGRESS: Parsing completed. Parsed 891 lines in 0.010159 secs.
{% endhighlight %}
<br/>

### Analyze
<br/>
{% highlight python %}
graphlab.canvas.set_target('ipynb')
passengers.show()
{% endhighlight %}
<div class="col three">
    <img class="col three" src="/img/titanic_ipython/pass1.png">
</div>

<br/>

###Pre process
<br/>
Age column has null values, fill it with Avg age

{% highlight python %}
passengers = passengers.fillna("Age",passengers["Age"].mean())
{% endhighlight %}
<br/>

###Feature engineering
<br/>
Consider the family size = 1 if (#siblings + #parents) > 3 else 0

{% highlight python %}
passengers['family'] = passengers['SibSp']+passengers['Parch'] >3
{% endhighlight %}
Create a new feature child, if the age is less than 15

{% highlight python %}
passengers["Child"] = passengers["Age"]<15
{% endhighlight %}
Extract title from Name

{% highlight python %}
import re
def findTitle(name):
    match = re.search("(Dr|Mrs?|Ms|Miss|Master|Rev|Capt|Mlle|Col|Major|Sir|Jonkheer|Lady|the Countess|Mme|Don)\\.",name)
    if match:
        title = match.group(0)
        if (title == 'Don.' or title == 'Major.' or title == 'Capt.'):
            title = 'Sir.'
        if (title == 'Mlle.' or title == 'Mme.'):
            title = 'Miss.'
        return title
    else:
        return "Other"
passengers["Title"] = passengers["Name"].apply(findTitle)
passengers["Title"].show()
{% endhighlight %}
<div class="col three">
    <img class="col three" src="/img/titanic_ipython/title.png">
</div>


**Feature binning**
<br/>
{% highlight python %}
from graphlab.toolkits.feature_engineering import *

binner = graphlab.feature_engineering.create(passengers, FeatureBinner(features = ['Fare'],strategy='quantile',num_bins = 5)) 
fit_binner = binner.fit(passengers)
passengers_binned = fit_binner.transform(passengers)
passengers_binned["Fare"].show()
{% endhighlight %}
<div class="col three">
    <img class="col three" src="/img/titanic_ipython/binning.png">
</div>

<br/>

###Feature selection
<br/>
{% highlight python %}
features = ["Pclass","Sex","Age","family","Child","Fare","Title"]
{% endhighlight %}
<br/>

###Model building
<br/>
Split data into train and test set

{% highlight python %}
train,test = passengers_binned.random_split(0.8,seed=0)


model = graphlab.logistic_classifier.create(passengers_binned,
                                            target="Survived",
                                            features = features,
                                            validation_set = test)

PROGRESS: Logistic regression:
PROGRESS: --------------------------------------------------------
PROGRESS: Number of examples          : 891
PROGRESS: Number of classes           : 2
PROGRESS: Number of feature columns   : 7
PROGRESS: Number of unpacked features : 7
PROGRESS: Number of coefficients    : 21
PROGRESS: Starting Newton Method
PROGRESS: --------------------------------------------------------
PROGRESS: +-----------+----------+--------------+-------------------+---------------------+
PROGRESS: | Iteration | Passes   | Elapsed Time | Training-accuracy | Validation-accuracy |
PROGRESS: +-----------+----------+--------------+-------------------+---------------------+
PROGRESS: | 1         | 2        | 0.002642     | 0.831650          | 0.781915            |
PROGRESS: | 2         | 3        | 0.004899     | 0.835017          | 0.781915            |
PROGRESS: | 3         | 4        | 0.007302     | 0.831650          | 0.776596            |
PROGRESS: | 4         | 5        | 0.009823     | 0.831650          | 0.776596            |
PROGRESS: | 5         | 6        | 0.012186     | 0.831650          | 0.776596            |
PROGRESS: | 6         | 7        | 0.014614     | 0.831650          | 0.776596            |
PROGRESS: +-----------+----------+--------------+-------------------+---------------------+
{% endhighlight %}
<br/>

###Evaluation
<br/>
ROC curve

{% highlight python %}
model.evaluate(test,metric='roc_curve')

{'roc_curve': Columns:
 	threshold	float
 	fpr	float
 	tpr	float
 	p	int
 	n	int
 
 Rows: 1001
 
 Data:
 +------------------+----------------+-----+----+-----+
 |    threshold     |      fpr       | tpr | p  |  n  |
 +------------------+----------------+-----+----+-----+
 |       0.0        |      0.0       | 0.0 | 75 | 113 |
 | 0.0010000000475  |      1.0       | 1.0 | 75 | 113 |
 | 0.00200000009499 |      1.0       | 1.0 | 75 | 113 |
 | 0.00300000002608 |      1.0       | 1.0 | 75 | 113 |
 | 0.00400000018999 |      1.0       | 1.0 | 75 | 113 |
 | 0.00499999988824 |      1.0       | 1.0 | 75 | 113 |
 | 0.00600000005215 | 0.982300884956 | 1.0 | 75 | 113 |
 | 0.00700000021607 | 0.982300884956 | 1.0 | 75 | 113 |
 | 0.00800000037998 | 0.982300884956 | 1.0 | 75 | 113 |
 | 0.00899999961257 | 0.982300884956 | 1.0 | 75 | 113 |
 +------------------+----------------+-----+----+-----+
 [1001 rows x 5 columns]
 Note: Only the head of the SFrame is printed.
 You can use print_rows(num_rows=m, num_columns=n) to print more rows and columns.}

model.show(view='Evaluation')
{% endhighlight %}
<div class="col three">
    <img class="col three" src="/img/titanic_ipython/roc.png">
</div>


<br/>

###Build model again using the entre input
<br/>
{% highlight python %}
model = graphlab.logistic_classifier.create(passengers_binned,
                                            target="Survived",
                                            features = features,
                                           validation_set = None)

PROGRESS: Logistic regression:
PROGRESS: --------------------------------------------------------
PROGRESS: Number of examples          : 891
PROGRESS: Number of classes           : 2
PROGRESS: Number of feature columns   : 7
PROGRESS: Number of unpacked features : 7
PROGRESS: Number of coefficients    : 21
PROGRESS: Starting Newton Method
PROGRESS: --------------------------------------------------------
PROGRESS: +-----------+----------+--------------+-------------------+
PROGRESS: | Iteration | Passes   | Elapsed Time | Training-accuracy |
PROGRESS: +-----------+----------+--------------+-------------------+
PROGRESS: | 1         | 2        | 0.002700     | 0.831650          |
PROGRESS: | 2         | 3        | 0.004679     | 0.835017          |
PROGRESS: | 3         | 4        | 0.006863     | 0.831650          |
PROGRESS: | 4         | 5        | 0.008501     | 0.831650          |
PROGRESS: | 5         | 6        | 0.010505     | 0.831650          |
PROGRESS: | 6         | 7        | 0.012663     | 0.831650          |
PROGRESS: +-----------+----------+--------------+-------------------+
{% endhighlight %}
<br/>

###Predict
<br/>
{% highlight python %}
passengers_submission = graphlab.SFrame('test.csv')

PROGRESS: Finished parsing file /Users/vishnu/git/hadoop/ipython/test.csv
PROGRESS: Parsing completed. Parsed 100 lines in 0.021006 secs.
------------------------------------------------------
Inferred types from first line of file as 
column_type_hints=[int,int,str,str,float,int,int,str,float,str,str]
If parsing fails due to incorrect types, you can correct
the inferred type list above and pass it to read_csv in
the column_type_hints argument
------------------------------------------------------
PROGRESS: Finished parsing file /Users/vishnu/git/hadoop/ipython/test.csv
PROGRESS: Parsing completed. Parsed 418 lines in 0.008928 secs.

passengers_submission.show()

passengers_submission['family'] = passengers_submission['SibSp']+passengers_submission['Parch'] >3
passengers_submission["Child"] = passengers_submission["Age"]<15
passengers_submission["Title"] = passengers_submission["Name"].apply(findTitle)
binner = graphlab.feature_engineering.create(passengers_submission, FeatureBinner(features = ['Fare'],strategy='quantile',num_bins = 5)) 
fit_binner = binner.fit(passengers_submission)
passengers_submission_binned = fit_binner.transform(passengers_submission)

passengers["Pclass","Sex","Age","family","Child","Fare","Title"].show()
{% endhighlight %}

<div class="col three">
    <img class="col three" src="/img/titanic_ipython/pass2.png">
</div>
{% highlight python %}
prediction = model.predict(passengers_submission_binned,output_type='class')
passengers_submission["Survived"] = prediction
result = passengers_submission["PassengerId","Survived"]
result
{% endhighlight %}



<div style="max-height:1000px;max-width:1500px;overflow:auto;"><table frame="box" rules="cols">
    <tr>
        <th style="padding-left: 1em; padding-right: 1em; text-align: center">PassengerId</th>
        <th style="padding-left: 1em; padding-right: 1em; text-align: center">Survived</th>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">892</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">0</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">893</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">1</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">894</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">0</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">895</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">0</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">896</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">1</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">897</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">0</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">898</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">1</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">899</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">0</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">900</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">1</td>
    </tr>
    <tr>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">901</td>
        <td style="padding-left: 1em; padding-right: 1em; text-align: center; vertical-align: top">0</td>
    </tr>
</table>
[418 rows x 2 columns]<br/>Note: Only the head of the SFrame is printed.<br/>You can use print_rows(num_rows=m, num_columns=n) to print more rows and columns.
</div>

<br/>
{% highlight python %}
result.save('submission.csv')
{% endhighlight %}
Received score of ***0.78469***
    
