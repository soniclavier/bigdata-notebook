---
layout: post
comments: true
title: Deep Learning - ANN, RNN, LSTM networks
date: 2017-12-17
PAGE_IDENTIFIER: ann_rnn_lstm
permalink: /ann_rnn_lstm.html
image: /img/lstm/header.png
tags: DeepLearning ANN RNN LSTM SequenceModeling Timeseries MachineLearning
has_math: true
description: Long Short Term Memory(LSTM) model is a type supervised Deep Neural Network that is very good at doing time-series prediction. In this blog, we do a step by step exploration of it's architecture starting from  the basic NN, then RNN leading to LSTM.
---
<div class="col three">
    <img class="col three" src="/img/lstm/header.png">
</div>
LSTM - Long Short Term Memory model is a type supervised Deep Neural Network that is very good at doing time-series prediction. It is a type of RNN (Recurrent Neural Network). An LSTM model looks at last "n" days(timestep) data (also called lag) and predicts how the series can progress in the future.

In this blog, we will try to understand how the layers in an LSTM model is connected with each other and understand the shape of weights, output and input matrices. We will not be looking at any particular implementation, that will be done in one of the future blog posts.  Let us begin by looking at the basic ANN model, then RNN and later on LSTM.

### **Artificial Neural Network (ANN)**
As you might already know, an ANN has an input layer, one or more hidden layer, and an output layer. In our example, we will consider a network with just one hidden layer with 2 neurons.

<div class="col three">
    <img class="col three expandable" src="/img/lstm/ann.png">
</div>

Each node in the input layer is connected to each node in the hidden layer and each node in the hidden layer is connected to each node in the output layer. All these connections have a weight associated with it, which is what a Neural Network learns during training - a set of weights that minimizes the overall cost of the model. Cost will be lowest if your prediction is close to the actual and will be high otherwise.

$$A_1 = \sigma(W_1X + b_1)$$

$$A_2 = \sigma(W_2A_1 + b_2)$$

\\(\sigma =\\) activation function.
\\(W_1, W_2, b_1, b_2 =\\) weights.
\\(A_1, A_2 =\\) activations.


Here we have one example with two features \\(x_1\\) and \\(x_2\\), hence \\(X\\) has the shape \\((2, 1)\\). Each of this input feature is connected to each of the 2 hidden layer nodes, hence the weight \\(W_1\\) has the shape \\((2, 2)\\). The bias unit \\(b1\\) is also connected with both the nodes in the hidden layer, so the shape of b1 is \\((2, 1)\\). The activation \\(A_1 = \sigma(W_1X + b_1)\\) will have the shape \\((2,1)\\). 


$$A_1(2,1) = W_1(2,2) \times X(2,1) + b_1(2,1)$$

Similarly, you can figure out the shapes of \\(b2, W2, A2\\). Understanding the relation between the shape of these matrices and the network architecture will help us later in figuring out the RNN and LSTM networks. Also notice that here we are dealing with only 1 example, suppose we have \\(m\\) such examples, then the shape equation holds true if we just replace all 1s with m. This helps in avoiding loops by vectorizing the computations.

$$A_1(2,m) = W_1(2,2) \times X(2,m) + b_1(2,m)$$

### **Recurrent Neural Network (RNN)**
RNN is a type of ANN, that has a recurring connection to itself. This recurring connection helps RNN learn the effect of previous input x(t-1) along with the current input x(t) while predicting the output at time "t" y(t). This gives RNN a sense of time context. The hidden layer activations calculated at time "t-1" are fed in as an input at time "t". 
<div class="col three">
    <img class="col three expandable" src="/img/lstm/rnn_1.png">
</div>
Above figure shows the high-level view of an RNN. We could also unroll the RNN's recurrent connection as shown in the figure. *Note: some Deep Learning libraries, such as Keras does not unroll the network by default since that requires more memory.* 
Here, \\(h(t), y(t)\\) stands for hidden state and output at time t. \\(h_t\\) and \\(y_t\\) are defined as:

$$h_t = \sigma(W_h x_t + U_h h_{t-1} + b_h )$$

$$y_t = \sigma(W_y h_t + b_y )$$

\\(\sigma\\) = the activation function.
\\(W_h, U_h, b_h, W_y, b_y\\) = weights.

The above equations can be put into perspective using the following figure (RNN with 2 units). As you can see, \\(h_{t-1}\\) is fed into at the network time \\(t\\) and is combined with \\(x_t\\) to produce \\(h_t\\) and \\(y_t\\). During back-propagation the model learns to adjust the weights \\(W_h, b_h, U_h, W_y, b_y \\)  which controls how much influence the current input and the past input(indirectly through \\(h_{t-1}\\)) has on the current output.
<div class="col three">
    <img class="col three expandable" src="/img/lstm/rnn_2.png">
</div>
*Note:* The inputs x1 and x2 are not two features, instead they are two timesteps(lag) of the same feature. This is a minor detail on how the input is structured for an RNN/LSTM model training that you should be aware of. Since we will be looking at last n(e.g., 2) timesteps(e.g., days) of data  to predict next m(e.g., 2) days of data for some feature \\(x\\), \\(X\\) and \\(Y\\) should be structured as

$$ 	X_1 = [x_{1},x_{2}], Y_1 = [x_{3}, x_{4}] $$

$$ 	X_2 = [x_{2},x_{3}], Y_2 = [x_{4}, x_{5}] $$

$$ 	X_3 = [x_{3},x_{4}], Y_3 = [x_{5}, x_{6}] $$

Will discuss more on this in future blogs, when we look at an implementation. In this example, we choose a lag of 2.

Let us now figure out the shapes of \\(W_h, U_h, b_h, W_y, b_y, h_t, y_t\\)

- shape of \\(W_h\\) = (2, 2) = (number of units, lag)
- shape of \\(U_h\\) = (2, 2) = (number of units, number of units)
- shape of \\(b_h\\) = (2, 1) = (number of units, 1)
- shape of \\(W_y\\) = (2, 2) = (number of units, number of units) 
- shape of \\(b_y\\) = (2, 1) = (number of units, 1)
- shape of \\(h_t\\)= (2, 1) = \\(W_h(2,2) \times x_t(2, 1) + U_h(2, 2) \times h_{t-1}(2, 1) + b_h(2, 1)\\)
- shape of \\(y_t\\) = (2, 1) = \\(W_y(2, 2) \times h_t(2, 1) + b_y(2, 1)\\)

#### **Vanishing gradient problem**
One of the problems with RNN networks is vanishing gradients, the gradients vanish to 0 during backpropagation. It arises because the derivative of the activation functions sigmoid(\\(\sigma\\)) or \\(tanh\\) are less than 0.25 and 1 respectively. And when many of these derivatives are multiplied together while applying chain rule, the gradients vanish to 0. This causes earlier layers to learn very slowly compared to later layers.
### **Long Short Term Memory (LSTM)**
LSTM model solves the problem of vanishing gradients by introducing a new state called cell state and having a CEC(Constant Error Carousel) which allows the error to propagate back without vanishing. For more details on what vanishing gradient problem is and how LSTM's CEC avoids this, watch out for the upcoming blog post [CEC in LSTM](#) here.

Forward pass in an LSTM cell: **(Use the slider to navigate)**
{% include _include_slider.html folder="lstm_slider" %}

#### **Need for gates**
**Forget gate** allows the model to learn when to clear(or partially clear) the contents of cell state. Intuitively this tells the model that the time context that it has been remembering is starting to get irrelevant. It might be now obvious the need of input and output gates, **input gate(i)** controls how much new information should be added to cell state, and **output gate(o)** controls when to use the contents in the cell state for producing \\(h_t\\), but the question "why it should be done?" remains. The reason is to avoid conflicts, e.g., for a weight *w* some of the inputs might try to pull the weights in one direction, where as some other input might try to pull it in another direction, in such cases these gates allows the model to control the weights update in such a way that it is updated in the direction where overall error is low.


LSTM's equations corresponding to the figure shown in the slides:

$$ f_t = \sigma(W_f x_t + U_f h_{t-1} + b_f) $$ 

$$ i_t = \sigma(W_i x_t + U_i h_{t-1} + b_i) $$ 

$$ o_t = \sigma(W_o x_t + U_o h_{t-1} + b_o) $$ 

$$ g_t = tanh(W_c x_t + U_c h_{t-1} + b_c) $$ 

$$ c_t = f_t \circ c_{t-1} + i_t \circ g_t $$

$$ h_t = o_t \circ \sigma(c_t) $$

'\\(\circ\\)' represents 'hadamard product' and \\(W_f, U_f, b_f, W_i\\) etc are the weights that LSTM learns during back propagation. We will now visualize the LSTM cell(with 2 units) as a network to see how are the inputs(with a lag of 2), weights and biases  wired with each other. 

- shape of \\(W\\) is \\((2 \times 2) = (units \times lag)\\)  
- shape of \\(U\\) is \\((2 \times 2) = (units \times units)\\)  
- shape of \\(b\\) is \\((2 \times 1) = (units \times 1)\\)  
- shape of \\(f_t, i_t, g_t\\) and \\(o_t\\) is \\((2 \times 1)\\) = \\(W (2 \times 2) \times x_t (2 \times 1) + U (2 \times 2) \times h_{t-1} (2 \times 1) + b (2 \times 1) \\) 
- shape of \\(c_t\\) is \\((2 \times 1) = f_t(2 \times 1) \circ c_{t-1}(2 \times 1) + i_t (2 \times 1) \circ g_t (2 \times 1) \\)
- shape of \\(h_t\\) is \\((2 \times 1) = o_t(2 \times 1) \circ c_t(2 \times 1) \\)

<div class="col three">
    <img class="col three expandable" src="/img/lstm/lstm_2_small.png">
</div>
[[View in high resolution]({{ site.baseurl }}/img/lstm/lstm_2.png)]

### **Conclusion**
LSTMs are always preferred over RNN since it is able to hold on to its memory for a longer period of time. In this blog post we focused mainly on the forward propagation, in the next post I will try to describe how back-propagation works and how CEC(Constant Error Carousel) avoids vanishing gradient problem. I hope this post helped you paint an overall picture of RNNs and LSTMs, or get a better understanding of what you already knew. And as always thank you for making till the end :)

#### References
[1] Sepp Hochreiter, Jurgen Schmidhuber. *Long Short Term Memory. Neural Computation,* 1997.
[2] Christopher Olah, *Understanding LSTM Networks*

[Home]({{ site.url }})

