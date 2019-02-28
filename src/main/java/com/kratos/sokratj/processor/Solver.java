package com.kratos.sokratj.processor;

import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.SlideShow;

import java.util.List;

/**
 * Solver
 *
 * @author Loic.Coulet
 */
public interface Solver {

    SlideShow getSolution(List<Photo> inputData);

}
